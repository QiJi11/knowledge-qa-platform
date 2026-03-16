package com.design.chat.service;

import com.design.chat.client.OpenAiChatClient;
import com.design.common.ApiException;
import com.design.faq.entity.Faq;
import com.design.faq.repository.FaqRepository;
import com.design.chat.model.ChatMessage;
import com.design.faq.service.FaqBloomFilterService;
import okhttp3.Call;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class ChatService {
  public static final String FALLBACK_ANSWER = "系统正在思考中，请稍后再试 🤔";

  private static final Logger log = LoggerFactory.getLogger(ChatService.class);

  private final FaqRepository faqRepository;
  private final OpenAiChatClient openAiChatClient;
  private final SessionService sessionService;
  private final ChatCacheService chatCacheService;
  private final FaqBloomFilterService faqBloomFilterService;

  public ChatService(
    FaqRepository faqRepository,
    OpenAiChatClient openAiChatClient,
    SessionService sessionService,
    ChatCacheService chatCacheService,
    FaqBloomFilterService faqBloomFilterService
  ) {
    this.faqRepository = faqRepository;
    this.openAiChatClient = openAiChatClient;
    this.sessionService = sessionService;
    this.chatCacheService = chatCacheService;
    this.faqBloomFilterService = faqBloomFilterService;
  }

  public String ask(String message, String sessionId) {
    List<ChatMessage> historyMessages = sessionService.getRecentMessages(sessionId);
    boolean useKnowledgeBase = faqBloomFilterService.mightContain(message);
    String cachedAnswer = chatCacheService.getCachedAnswer(message);
    if (cachedAnswer != null && !cachedAnswer.isBlank()) {
      sessionService.appendConversation(sessionId, message, cachedAnswer);
      return cachedAnswer;
    }

    List<Faq> matchedFaqs = useKnowledgeBase ? faqRepository.searchByKeywords(message) : List.of();
    String systemPrompt = buildSystemPrompt(sessionId, matchedFaqs);
    String answer;

    try {
      answer = openAiChatClient.ask(systemPrompt, historyMessages, message);
    } catch (OpenAiChatClient.AiClientTimeoutException e) {
      log.warn("AI 调用超时，sessionId={}", sessionId, e);
      answer = FALLBACK_ANSWER;
    } catch (OpenAiChatClient.AiClientUnavailableException e) {
      // API Key 未配置时不降级，直接向上抛出让 GlobalExceptionHandler 返回错误码
      if (e.getMessage() != null && e.getMessage().contains("未配置有效的")) {
        throw ApiException.badRequest(e.getMessage());
      }
      log.warn("AI 调用不可用，sessionId={}, message={}", sessionId, e.getMessage());
      answer = FALLBACK_ANSWER;
    }

    if (!FALLBACK_ANSWER.equals(answer)) {
      chatCacheService.cacheAnswer(message, answer);
    }
    sessionService.appendConversation(sessionId, message, answer);
    return answer;
  }

  public SseEmitter askStream(String message, String sessionId) {
    SseEmitter emitter = new SseEmitter(15000L); // 15s超时兜底防卡死
    List<ChatMessage> historyMessages = sessionService.getRecentMessages(sessionId);
    String systemPrompt = buildSystemPrompt(sessionId, faqRepository.searchByKeywords(message));

    AtomicBoolean sentToken = new AtomicBoolean(false);
    AtomicBoolean finished = new AtomicBoolean(false);
    AtomicBoolean persisted = new AtomicBoolean(false);
    AtomicReference<Call> callRef = new AtomicReference<>();
    StringBuilder answerBuilder = new StringBuilder();

    emitter.onCompletion(() -> cancelCall(callRef));
    emitter.onTimeout(() -> {
      log.warn("SSE 连接超时，sessionId={}", sessionId);
      handleStreamFailure(emitter, sessionId, message, sentToken, finished, persisted, answerBuilder, callRef, null);
    });
    emitter.onError(error -> {
      log.warn("SSE 连接异常，sessionId={}", sessionId, error);
      handleStreamFailure(
        emitter,
        sessionId,
        message,
        sentToken,
        finished,
        persisted,
        answerBuilder,
        callRef,
        error
      );
    });

    try {
      Call call =
        openAiChatClient.askStream(
          systemPrompt,
          historyMessages,
          message,
          new OpenAiChatClient.StreamHandler() {
            @Override
            public void onToken(String token) {
              if (finished.get()) {
                return;
              }

              try {
                emitter.send(token);
                sentToken.set(true);
                answerBuilder.append(token);
              } catch (IOException e) {
                log.warn("SSE 推送 token 失败，sessionId={}", sessionId, e);
                handleStreamFailure(
                  emitter,
                  sessionId,
                  message,
                  sentToken,
                  finished,
                  persisted,
                  answerBuilder,
                  callRef,
                  e
                );
              }
            }

            @Override
            public void onComplete() {
              if (!sentToken.get()) {
                answerBuilder.setLength(0);
                answerBuilder.append(FALLBACK_ANSWER);
                try {
                  emitter.send(FALLBACK_ANSWER);
                  sentToken.set(true);
                } catch (IOException e) {
                  log.debug("SSE 空响应兜底发送失败，sessionId={}", sessionId, e);
                }
              }

              persistConversation(sessionId, message, answerBuilder.toString(), persisted);
              completeStream(emitter, finished, callRef);
            }

            @Override
            public void onError(RuntimeException exception) {
              handleStreamFailure(
                emitter,
                sessionId,
                message,
                sentToken,
                finished,
                persisted,
                answerBuilder,
                callRef,
                exception
              );
            }
          }
        );
      callRef.set(call);
    } catch (OpenAiChatClient.AiClientUnavailableException e) {
      log.warn("AI 流式调用不可用，sessionId={}, message={}", sessionId, e.getMessage());
      handleStreamFailure(
        emitter,
        sessionId,
        message,
        sentToken,
        finished,
        persisted,
        answerBuilder,
        callRef,
        e
      );
    }

    return emitter;
  }

  private String buildSystemPrompt(String sessionId, List<Faq> matchedFaqs) {
    StringBuilder prompt = new StringBuilder();
    prompt.append("你是一个在线学习平台的AI学习助手。你的职责是：1)推荐合适的课程；2)回答技术问题；3)解答平台使用问题。");
    prompt.append("请用简洁友好的中文回答。对于日常问候请热情回复。");
    prompt.append("当前会话ID：").append(sessionId).append("。");

    if (matchedFaqs.isEmpty()) {
      prompt.append("知识库暂无匹配内容。请根据通用知识回答，但不要编造本平台的具体课程名称或价格。如果用户问课程推荐，请引导用户到课程列表页浏览。");
      return prompt.toString();
    }

    prompt.append("以下是知识库中与用户问题相关的参考资料，请优先基于这些资料回答：");

    int maxFaqCount = Math.min(matchedFaqs.size(), 5);
    for (int i = 0; i < maxFaqCount; i++) {
      Faq faq = matchedFaqs.get(i);
      prompt
        .append("\n")
        .append(i + 1)
        .append(". 问题：")
        .append(faq.getQuestion())
        .append("\n   关键词：")
        .append(faq.getKeywords() == null ? "-" : faq.getKeywords())
        .append("\n   分类：")
        .append(faq.getCategory() == null ? "-" : faq.getCategory())
        .append("\n   回答：")
        .append(faq.getAnswer());
    }

    return prompt.toString();
  }

  private void handleStreamFailure(
    SseEmitter emitter,
    String sessionId,
    String userMessage,
    AtomicBoolean sentToken,
    AtomicBoolean finished,
    AtomicBoolean persisted,
    StringBuilder answerBuilder,
    AtomicReference<Call> callRef,
    Throwable error
  ) {
    if (finished.get()) {
      return;
    }

    if (error != null) {
      log.warn("AI 流式调用结束异常，sessionId={}", sessionId, error);
    }

    if (!sentToken.get()) {
      answerBuilder.setLength(0);
      answerBuilder.append(FALLBACK_ANSWER);
      try {
        emitter.send(FALLBACK_ANSWER);
        sentToken.set(true);
      } catch (IOException e) {
        log.debug("SSE 兜底文案发送失败，sessionId={}", sessionId, e);
      }
    }

    persistConversation(sessionId, userMessage, answerBuilder.toString(), persisted);
    completeStream(emitter, finished, callRef);
  }

  private void completeStream(SseEmitter emitter, AtomicBoolean finished, AtomicReference<Call> callRef) {
    if (!finished.compareAndSet(false, true)) {
      return;
    }

    try {
      emitter.send("[DONE]");
    } catch (IOException e) {
      log.debug("SSE [DONE] 发送失败", e);
    } finally {
      cancelCall(callRef);
      emitter.complete();
    }
  }

  private void persistConversation(
    String sessionId,
    String userMessage,
    String assistantMessage,
    AtomicBoolean persisted
  ) {
    if (!persisted.compareAndSet(false, true)) {
      return;
    }

    String resolvedAssistantMessage = assistantMessage;
    if (resolvedAssistantMessage == null || resolvedAssistantMessage.isBlank()) {
      resolvedAssistantMessage = FALLBACK_ANSWER;
    }

    sessionService.appendConversation(sessionId, userMessage, resolvedAssistantMessage);
  }

  private void cancelCall(AtomicReference<Call> callRef) {
    Call call = callRef.get();
    if (call != null && !call.isCanceled()) {
      call.cancel();
    }
  }
}
