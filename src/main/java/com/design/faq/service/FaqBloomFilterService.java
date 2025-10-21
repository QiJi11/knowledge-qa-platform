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

  @PostConstruct
  public synchronized void init() {
    bloomFilter = BloomFilter.create(Funnels.stringFunnel(StandardCharsets.UTF_8), 10000, 0.01);
    filterReady = false;

    try {
      List<String> questions = faqRepository.findAllQuestions();
      for (String question : questions) {
        putInternal(question);
      }
      filterReady = true;
      log.info("FAQ BloomFilter 初始化完成，加载问题数量={}", questions.size());
    } catch (Exception e) {
      log.warn("FAQ BloomFilter 初始化失败，将回退为始终执行知识库检索", e);
    }
  }

  public synchronized boolean mightContain(String question) {
    if (!filterReady) {
      return true;
    }

    String normalizedQuestion = normalize(question);
    if (normalizedQuestion == null) {
      return true;
    }

    return bloomFilter.mightContain(normalizedQuestion);
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
