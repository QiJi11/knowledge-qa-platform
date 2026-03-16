package com.design.faq.service;

import com.design.faq.repository.FaqRepository;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class FaqBloomFilterService {
  private static final Logger log = LoggerFactory.getLogger(FaqBloomFilterService.class);

  private final FaqRepository faqRepository;

  private BloomFilter<CharSequence> bloomFilter;
  private volatile boolean filterReady;

  public FaqBloomFilterService(FaqRepository faqRepository) {
    this.faqRepository = faqRepository;
  }

  private int faqCount;

  @PostConstruct
  public synchronized void init() {
    bloomFilter = BloomFilter.create(Funnels.stringFunnel(StandardCharsets.UTF_8), 10000, 0.01);
    filterReady = false;
    faqCount = 0;

    try {
      List<String> questions = faqRepository.findAllQuestions();
      faqCount = questions.size();
      for (String question : questions) {
        // 把问题拆分成关键词加入 BloomFilter
        if (question != null) {
          for (String token : question.split("[\\s,，。？?！!、;；：:]+")) {
            String t = token.trim();
            if (t.length() >= 2) {
              bloomFilter.put(t);
            }
          }
        }
      }
      filterReady = true;
      log.info("FAQ BloomFilter 初始化完成，加载问题数量={}", faqCount);
    } catch (Exception e) {
      log.warn("FAQ BloomFilter 初始化失败，将回退为始终执行知识库检索", e);
    }
  }

  public synchronized boolean mightContain(String question) {
    // FAQ 数量少于 100 条时，直接搜索知识库，跳过 BloomFilter 过滤
    if (!filterReady || faqCount < 100) {
      return true;
    }

    if (question == null || question.isBlank()) {
      return true;
    }

    // 按分词检查：只要有任何一个 token 命中 BloomFilter 就搜索知识库
    String[] tokens = question.trim().split("[\\s,，。？?！!、;；：:]+");
    for (String token : tokens) {
      String t = token.trim();
      if (t.length() >= 2 && bloomFilter.mightContain(t)) {
        return true;
      }
    }
    return false;
  }

  public synchronized void addQuestion(String question) {
    putInternal(question);
  }

  private void putInternal(String question) {
    String normalizedQuestion = normalize(question);
    if (normalizedQuestion == null) {
      return;
    }

    bloomFilter.put(normalizedQuestion);
  }

  private String normalize(String question) {
    if (question == null) {
      return null;
    }

    String normalizedQuestion = question.trim();
    return normalizedQuestion.isEmpty() ? null : normalizedQuestion;
  }
}
