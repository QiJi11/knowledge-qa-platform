package com.design.faq.controller;

import com.design.faq.dto.FaqUpsertRequest;
import com.design.faq.entity.Faq;
import com.design.faq.repository.FaqRepository;
import com.design.faq.service.FaqService;
import com.design.common.ApiException;
import com.design.common.Result;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@Validated
@RestController("faqV1Controller")
@RequestMapping("/api/v1/faq")
public class FaqController {
  private final FaqService service;

  public FaqController(FaqService service) {
    this.service = service;
  }

  private static long parseFaqIdOrThrow(String raw) {
    if (raw == null || raw.isBlank()) {
      throw ApiException.badRequest("faqId 必须是正整数");
    }

    try {
      long id = Long.parseLong(raw.trim());
      if (id <= 0) {
        throw ApiException.badRequest("faqId 必须是正整数");
      }
      return id;
    } catch (NumberFormatException e) {
      throw ApiException.badRequest("faqId 必须是正整数");
    }
  }

  private static String normalizeOptionalString(String value) {
    if (value == null) {
      return null;
    }

    String resolvedValue = value.trim();
    return resolvedValue.isEmpty() ? null : resolvedValue;
  }

  @PostMapping
  public Result<Faq> createFaq(@Valid @RequestBody FaqUpsertRequest request) {
    Faq created =
      service.createFaq(
        request.getQuestion().trim(),
        request.getAnswer().trim(),
        normalizeOptionalString(request.getKeywords()),
        normalizeOptionalString(request.getCategory()),
        request.getHitCount()
      );
    return Result.ok(created);
  }

  @GetMapping("/{faqId}")
  public Result<Faq> getFaq(@PathVariable("faqId") String faqId) {
    long id = parseFaqIdOrThrow(faqId);

    Faq faq = service.getFaq(id);
    if (faq == null) {
      throw ApiException.notFound("未找到该 FAQ");
    }

    return Result.ok(faq);
  }

  @GetMapping
  public Result<Map<String, Object>> listFaqs(
    @RequestParam(required = false) String keyword,
    @RequestParam(required = false) Integer page,
    @RequestParam(required = false) Integer pageSize
  ) {
    int resolvedPage = page == null ? 1 : page;
    if (resolvedPage < 1) {
      throw ApiException.badRequest("page 必须大于等于 1");
    }

    int resolvedPageSize = pageSize == null ? 20 : pageSize;
    if (resolvedPageSize <= 0) {
      throw ApiException.badRequest("pageSize 必须大于 0");
    }
    if (resolvedPageSize > 50) {
      throw ApiException.badRequest("pageSize 不能超过 50");
    }

    FaqRepository.FaqPage result = service.listFaqs(keyword, resolvedPage, resolvedPageSize);

    Map<String, Object> data = new HashMap<>();
    data.put("items", result.getItems());
    data.put("page", result.getPage());
    data.put("pageSize", result.getPageSize());
    data.put("total", result.getTotal());

    return Result.ok(data);
  }

  @PutMapping("/{faqId}")
  public Result<Faq> updateFaq(@PathVariable("faqId") String faqId, @Valid @RequestBody FaqUpsertRequest request) {
    long id = parseFaqIdOrThrow(faqId);

    Faq updated =
      service.updateFaq(
        id,
        request.getQuestion().trim(),
        request.getAnswer().trim(),
        normalizeOptionalString(request.getKeywords()),
        normalizeOptionalString(request.getCategory()),
        request.getHitCount()
      );
    if (updated == null) {
      throw ApiException.notFound("未找到该 FAQ");
    }

    return Result.ok(updated);
  }

  @DeleteMapping("/{faqId}")
  public Result<Map<String, Object>> deleteFaq(@PathVariable("faqId") String faqId) {
    long id = parseFaqIdOrThrow(faqId);
    service.deleteFaq(id);

    Map<String, Object> data = new HashMap<>();
    data.put("deleted", true);
    return Result.ok(data);
  }
}
