package com.design.chat.client;

import com.design.chat.model.ChatMessage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.List;

@Component
public class OpenAiChatClient {
  private static final Logger log = LoggerFactory.getLogger(OpenAiChatClient.class);
  private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

  private final OkHttpClient httpClient;
  private final ObjectMapper objectMapper;
  private final String apiKey;
  private final String baseUrl;
  private final String model;

  public OpenAiChatClient(
    OkHttpClient httpClient,
    ObjectMapper objectMapper,
    @Value("${spring.ai.openai.api-key}") String apiKey,
    @Value("${spring.ai.openai.base-url}") String baseUrl,
    @Value("${spring.ai.openai.chat.options.model}") String model
  ) {
    this.httpClient = httpClient;
    this.objectMapper = objectMapper;
    this.apiKey = apiKey;
    this.baseUrl = baseUrl;
    this.model = model;
  }

  public String ask(String systemPrompt, List<ChatMessage> historyMessages, String userMessage) {
    Request request = buildChatRequest(systemPrompt, historyMessages, userMessage, false);

    try (Response response = httpClient.newCall(request).execute()) {
      String responseBody = response.body() == null ? "" : response.body().string();
      if (!response.isSuccessful()) {
        throw new AiClientUnavailableException(resolveErrorMessage(response.code(), responseBody));
      }
      return extractAnswer(responseBody);
    } catch (InterruptedIOException e) {
      log.warn("AI 调用超时", e);
      throw new AiClientTimeoutException("AI 调用超时", e);
    } catch (IOException e) {
      throw new AiClientUnavailableException("AI 调用失败：" + e.getMessage(), e);
    }
  }

  public Call askStream(
    String systemPrompt,
    List<ChatMessage> historyMessages,
    String userMessage,
    StreamHandler handler
  ) {
    Request request = buildChatRequest(systemPrompt, historyMessages, userMessage, true);
    Call call = httpClient.newCall(request);

    call.enqueue(
      new Callback() {
        @Override
        public void onFailure(Call call, IOException e) {
          if (call.isCanceled()) {
            log.debug("AI 流式调用已取消");
            return;
          }

          if (e instanceof InterruptedIOException) {
            handler.onError(new AiClientTimeoutException("AI 流式调用超时", e));
            return;
          }

          handler.onError(new AiClientUnavailableException("AI 流式调用失败：" + e.getMessage(), e));
        }

        @Override
        public void onResponse(Call call, Response response) {
          try (Response ignored = response) {
            if (response.body() == null) {
              handler.onError(new AiClientUnavailableException("AI 流式响应体为空"));
              return;
            }

            if (!response.isSuccessful()) {
              String responseBody = response.body().string();
              handler.onError(new AiClientUnavailableException(resolveErrorMessage(response.code(), responseBody)));
              return;
            }

            BufferedReader reader = new BufferedReader(response.body().charStream());
            String line;
            while ((line = reader.readLine()) != null) {
              if (line == null || line.isBlank() || !line.startsWith("data:")) {
                continue;
              }

              String data = line.substring("data:".length()).trim();
              if (data.isBlank()) {
                continue;
              }

              if ("[DONE]".equals(data)) {
                handler.onComplete();
                return;
              }

              JsonNode root = objectMapper.readTree(data);
              String token = extractContent(root.path("choices").path(0).path("delta").path("content"));
              if (token != null && !token.isBlank()) {
                handler.onToken(token);
              }
            }

            handler.onComplete();
          } catch (InterruptedIOException e) {
            handler.onError(new AiClientTimeoutException("AI 流式调用超时", e));
          } catch (Exception e) {
            if (call.isCanceled()) {
              log.debug("AI 流式调用在解析响应时被取消");
              return;
            }
            handler.onError(new AiClientUnavailableException("解析 AI 流式响应失败：" + e.getMessage(), e));
          }
        }
      }
    );

    return call;
  }

  private Request buildChatRequest(
    String systemPrompt,
    List<ChatMessage> historyMessages,
    String userMessage,
    boolean stream
  ) {
    if (apiKey == null || apiKey.isBlank() || "sk-placeholder".equals(apiKey.trim())) {
      throw new AiClientUnavailableException("未配置有效的 OPENAI_API_KEY");
    }

    ObjectNode payload = objectMapper.createObjectNode();
    payload.put("model", model);
    payload.put("temperature", 0.2);
    if (stream) {
      payload.put("stream", true);
    }

    ArrayNode messages = payload.putArray("messages");
    ObjectNode developerMessage = messages.addObject();
    developerMessage.put("role", "developer");
    developerMessage.put("content", systemPrompt);

    appendHistoryMessages(messages, historyMessages);

    ObjectNode userNode = messages.addObject();
    userNode.put("role", "user");
    userNode.put("content", userMessage);

    return
      new Request.Builder()
        .url(resolveChatUrl())
        .header("Authorization", "Bearer " + apiKey.trim())
        .header("Content-Type", "application/json")
        .post(RequestBody.create(payload.toString(), JSON))
        .build();
  }

  private void appendHistoryMessages(ArrayNode messages, List<ChatMessage> historyMessages) {
    if (historyMessages == null || historyMessages.isEmpty()) {
      return;
    }

    for (ChatMessage historyMessage : historyMessages) {
      if (historyMessage == null) {
        continue;
      }

      String role = historyMessage.getRole();
      String content = historyMessage.getContent();
      if (role == null || content == null || content.isBlank()) {
        continue;
      }

      if (!"user".equals(role) && !"assistant".equals(role)) {
        continue;
      }

      ObjectNode messageNode = messages.addObject();
      messageNode.put("role", role);
      messageNode.put("content", content);
    }
  }

  private String resolveChatUrl() {
    String resolvedBaseUrl = baseUrl == null ? "https://api.openai.com" : baseUrl.trim();
    if (resolvedBaseUrl.endsWith("/")) {
      resolvedBaseUrl = resolvedBaseUrl.substring(0, resolvedBaseUrl.length() - 1);
    }
    return resolvedBaseUrl + "/v1/chat/completions";
  }

  private String extractAnswer(String responseBody) {
    try {
      JsonNode root = objectMapper.readTree(responseBody);
      JsonNode contentNode = root.path("choices").path(0).path("message").path("content");

      String answer = extractContent(contentNode);
      if (answer == null || answer.isBlank()) {
        throw new AiClientUnavailableException("AI 返回内容为空");
      }

      return answer.trim();
    } catch (IOException e) {
      throw new AiClientUnavailableException("解析 AI 响应失败：" + e.getMessage(), e);
    }
  }

  private String extractContent(JsonNode contentNode) {
    if (contentNode == null || contentNode.isMissingNode() || contentNode.isNull()) {
      return null;
    }

    if (contentNode.isTextual()) {
      return contentNode.asText();
    }

    if (!contentNode.isArray()) {
      return null;
    }

    StringBuilder builder = new StringBuilder();
    for (JsonNode item : contentNode) {
      if (item.isTextual()) {
        builder.append(item.asText());
        continue;
      }

      if ("text".equals(item.path("type").asText()) && item.hasNonNull("text")) {
        builder.append(item.get("text").asText());
      }
    }

    return builder.toString();
  }

  private String resolveErrorMessage(int statusCode, String responseBody) {
    if (responseBody != null && !responseBody.isBlank()) {
      try {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode errorNode = root.path("error").path("message");
        if (errorNode.isTextual() && !errorNode.asText().isBlank()) {
          return "AI 调用失败(" + statusCode + ")：" + errorNode.asText();
        }
      } catch (IOException ignored) {
        // Keep the generic fallback error message below.
      }
    }

    return "AI 调用失败(" + statusCode + ")";
  }

  public static class AiClientUnavailableException extends RuntimeException {
    public AiClientUnavailableException(String message) {
      super(message);
    }

    public AiClientUnavailableException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  public static class AiClientTimeoutException extends RuntimeException {
    public AiClientTimeoutException(String message, Throwable cause) {
      super(message, cause);
    }
  }

  public interface StreamHandler {
    void onToken(String token);

    void onComplete();

    void onError(RuntimeException exception);
  }
}
