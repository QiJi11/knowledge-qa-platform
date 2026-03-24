package com.design.faq.service;

import com.design.faq.entity.Faq;
import com.design.faq.repository.FaqRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * FAQ 向量检索服务
 * 使用 OpenAI text-embedding 接口对 FAQ 问题做向量化，
 * 存入内存（Map<Long, float[]>），检索时对用户问题做余弦相似度匹配。
 *
 * 当 API Key 不可用时自动降级到关键词检索（FaqRepository.searchByKeywords）。
 */
@Service
public class FaqEmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(FaqEmbeddingService.class);

    /** FAQ id → embedding 向量 */
    private final Map<Long, float[]> vectorIndex = new ConcurrentHashMap<>();
    /** FAQ id → Faq 对象 */
    private final Map<Long, Faq> faqCache = new ConcurrentHashMap<>();

    private final FaqRepository faqRepository;

    // 直接读取环境变量，与 OpenAiChatClient 保持一致
    private final String apiKey = System.getenv().getOrDefault("OPENAI_API_KEY", "");
    private final String embeddingModel = "text-embedding-3-small";
    private final String embeddingUrl = "https://api.openai.com/v1/embeddings";

    public FaqEmbeddingService(FaqRepository faqRepository) {
        this.faqRepository = faqRepository;
    }

    /**
     * 应用启动后异步加载全量 FAQ 的向量索引。
     * 若 API Key 为空则跳过（降级到关键词检索）。
     */
    @PostConstruct
    public void buildIndex() {
        if (apiKey.isBlank()) {
            log.info("[EmbeddingService] OPENAI_API_KEY 未配置，跳过向量索引构建（将降级为关键词检索）");
            return;
        }
        new Thread(() -> {
            try {
                log.info("[EmbeddingService] 开始构建 FAQ 向量索引...");
                List<Faq> allFaqs = faqRepository.findAll();
                int success = 0;
                for (Faq faq : allFaqs) {
                    float[] vec = embed(faq.getQuestion());
                    if (vec != null) {
                        vectorIndex.put(faq.getId(), vec);
                        faqCache.put(faq.getId(), faq);
                        success++;
                    }
                }
                log.info("[EmbeddingService] 向量索引构建完成，共 {} 条", success);
            } catch (Exception e) {
                log.warn("[EmbeddingService] 向量索引构建失败：{}", e.getMessage());
            }
        }, "embedding-index-builder").start();
    }

    /**
     * 新增/更新单条 FAQ 时同步更新向量索引。
     */
    public void indexFaq(Faq faq) {
        if (apiKey.isBlank()) return;
        new Thread(() -> {
            float[] vec = embed(faq.getQuestion());
            if (vec != null) {
                vectorIndex.put(faq.getId(), vec);
                faqCache.put(faq.getId(), faq);
                log.debug("[EmbeddingService] 已索引 FAQ id={}", faq.getId());
            }
        }).start();
    }

    /**
     * 删除 FAQ 时同步移除向量索引。
     */
    public void removeFaq(long id) {
        vectorIndex.remove(id);
        faqCache.remove(id);
    }

    /**
     * 向量相似度检索，返回 top-k 相关 FAQ。
     * 若向量索引未就绪，降级调用 faqRepository.searchByKeywords。
     */
    public List<Faq> search(String query, int topK) {
        if (vectorIndex.isEmpty() || apiKey.isBlank()) {
            log.debug("[EmbeddingService] 向量索引未就绪，降级到关键词检索");
            return faqRepository.searchByKeywords(query);
        }

        float[] queryVec = embed(query);
        if (queryVec == null) {
            return faqRepository.searchByKeywords(query);
        }

        // 计算所有 FAQ 与 query 的余弦相似度，取 top-k
        List<Map.Entry<Long, Float>> scores = new ArrayList<>();
        for (Map.Entry<Long, float[]> entry : vectorIndex.entrySet()) {
            float sim = cosineSimilarity(queryVec, entry.getValue());
            scores.add(Map.entry(entry.getKey(), sim));
        }
        scores.sort((a, b) -> Float.compare(b.getValue(), a.getValue()));

        List<Faq> result = new ArrayList<>();
        for (int i = 0; i < Math.min(topK, scores.size()); i++) {
            float score = scores.get(i).getValue();
            if (score < 0.3f) break; // 相似度过低则截断
            Faq faq = faqCache.get(scores.get(i).getKey());
            if (faq != null) result.add(faq);
        }

        log.debug("[EmbeddingService] 向量检索完成，query='{}', 命中 {} 条", query, result.size());
        return result;
    }

    // ======================== 私有方法 ========================

    /**
     * 调用 OpenAI Embedding API 获取向量。
     */
    private float[] embed(String text) {
        try {
            String body = "{\"model\":\"" + embeddingModel + "\",\"input\":\""
                + escapeJson(text) + "\"}";

            URL url = new URL(embeddingUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(10000);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            if (conn.getResponseCode() != 200) {
                log.warn("[EmbeddingService] API 返回 HTTP {}", conn.getResponseCode());
                return null;
            }

            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
            }

            return parseEmbedding(sb.toString());
        } catch (Exception e) {
            log.debug("[EmbeddingService] embed 失败：{}", e.getMessage());
            return null;
        }
    }

    /**
     * 简单解析 OpenAI embedding response 中的 float 数组。
     * 格式: {...,"embedding":[0.1,0.2,...],...}
     */
    private float[] parseEmbedding(String json) {
        int start = json.indexOf("\"embedding\":[");
        if (start == -1) return null;
        start += "\"embedding\":[".length();
        int end = json.indexOf(']', start);
        if (end == -1) return null;
        String[] parts = json.substring(start, end).split(",");
        float[] vec = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            vec[i] = Float.parseFloat(parts[i].trim());
        }
        return vec;
    }

    /**
     * 余弦相似度：cos(θ) = (a·b) / (|a| * |b|)
     */
    private float cosineSimilarity(float[] a, float[] b) {
        if (a.length != b.length) return 0f;
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        double denom = Math.sqrt(normA) * Math.sqrt(normB);
        return denom == 0 ? 0f : (float) (dot / denom);
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r");
    }
}
