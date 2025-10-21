package com.design.faq.service;

import com.design.faq.entity.Faq;
import com.design.faq.repository.FaqRepository;
import com.design.todo.api.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("faqV1Service")
public class FaqService {
  private final FaqRepository repo;
  private final FaqBloomFilterService faqBloomFilterService;

  public FaqService(FaqRepository repo, FaqBloomFilterService faqBloomFilterService) {
    this.repo = repo;
    this.faqBloomFilterService = faqBloomFilterService;
  }

  public Faq getFaq(long id) {
    return repo.getFaq(id);
  }

  public FaqRepository.FaqPage listFaqs(String keyword, int page, int pageSize) {
    return repo.listFaqs(keyword, page, pageSize);
  }

  public List<Faq> searchByKeywords(String keyword) {
    return repo.searchByKeywords(keyword);
  }

  @Transactional
  public Faq createFaq(String question, String answer, String keywords, String category, Integer hitCount) {
    int resolvedHitCount = hitCount == null ? 0 : hitCount;
    Faq created = repo.createFaq(question, answer, keywords, category, resolvedHitCount);
    faqBloomFilterService.addQuestion(created.getQuestion());
    return created;
  }

  @Transactional
  public Faq updateFaq(long id, String question, String answer, String keywords, String category, Integer hitCount) {
    Faq existing = repo.getFaq(id);
    if (existing == null) return null;

    int resolvedHitCount = hitCount == null ? existing.getHitCount() : hitCount;
    Faq updated = repo.updateFaq(id, question, answer, keywords, category, resolvedHitCount);
    if (updated != null) {
      faqBloomFilterService.addQuestion(updated.getQuestion());
    }
    return updated;
  }

  @Transactional
  public void deleteFaq(long id) {
    boolean deleted = repo.deleteFaq(id);
    if (!deleted) {
      throw ApiException.notFound("未找到该 FAQ");
    }
  }
}
