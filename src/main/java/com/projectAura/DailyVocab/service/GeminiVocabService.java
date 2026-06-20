package com.projectAura.DailyVocab.service;

import com.projectAura.DailyVocab.model.VocabWord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
public class GeminiVocabService {

    private static final Logger log = LoggerFactory.getLogger(GeminiVocabService.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    @Value("${gemini.api.key}")
    private String apiKey;

    private static final String GEMINI_BASE_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent";

    // Models tried in order; if the primary is overloaded/quota-exceeded, fallback kicks in
    private static final List<String> MODELS = List.of(
            "gemini-2.5-flash",
            "gemini-2.0-flash",
            "gemini-2.5-flash-lite"
    );

    public GeminiVocabService(ObjectMapper objectMapper) {
        this.restClient = RestClient.create();
        this.objectMapper = objectMapper;
    }

    public List<VocabWord> generateVocabWords() {
        int maxRetries = 3;
        Exception lastException = null;

        for (String model : MODELS) {
            log.info("Trying Gemini model: {}", model);
            for (int attempt = 1; attempt <= maxRetries; attempt++) {
                try {
                    return callGemini(model);
                } catch (HttpClientErrorException.TooManyRequests e) {
                    // 429 — quota exhausted, no point retrying this model
                    lastException = e;
                    log.warn("Model {} quota exhausted (429), skipping to next model.", model);
                    break;
                } catch (HttpClientErrorException.NotFound e) {
                    // 404 — model not available, skip immediately
                    lastException = e;
                    log.warn("Model {} not found (404), skipping to next model.", model);
                    break;
                } catch (HttpServerErrorException.ServiceUnavailable e) {
                    // 503 — model overloaded, exponential backoff then try next model
                    lastException = e;
                    log.warn("Model {} attempt {}/{} got 503 (overloaded)", model, attempt, maxRetries);
                    if (attempt < maxRetries) {
                        sleepSilently(3000L * attempt); // 3s, 6s
                    }
                } catch (Exception e) {
                    lastException = e;
                    log.warn("Model {} attempt {}/{} failed: {}", model, attempt, maxRetries, e.getMessage());
                    if (attempt < maxRetries) {
                        sleepSilently(3000L * attempt);
                    }
                }
            }
            log.warn("Model {} exhausted all retries, trying next fallback model.", model);
        }
        throw new RuntimeException("All Gemini models failed. Last error: " +
                (lastException != null ? lastException.getMessage() : "unknown"), lastException);
    }

    private void sleepSilently(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
    }

    private List<VocabWord> callGemini(String model) {
        String url = String.format(GEMINI_BASE_URL, model) + "?key=" + apiKey;

        String requestBody = """
                {
                  "contents": [{
                    "parts": [{
                      "text": "Generate exactly 5 English vocabulary words suitable for an Indian English speaker who wants to improve their conversational vocabulary. Choose words that are practical and used in everyday English conversations, yet are slightly uncommon for someone whose first language is not English. Target words at the level of: vivid, fragile, diligent, abundant, resilient, subtle, sincere, persist, concise, motive. Avoid very simple words (happy, good, fast) and avoid overly rare or literary words (petrichor, ephemeral, sycophant, ubiquitous). The words should be ones a person might encounter in English news, casual conversations, emails, or TV shows. Return ONLY a valid JSON array with no markdown, no code blocks, no explanation. Each object must have exactly these fields: word, part_of_speech, meaning, example_sentence, synonyms (array of 2-3 strings). Example format: [{\\"word\\": \\"diligent\\", \\"part_of_speech\\": \\"adjective\\", \\"meaning\\": \\"Showing steady and careful effort in work or duties.\\", \\"example_sentence\\": \\"She was diligent in completing her assignments before the deadline.\\", \\"synonyms\\": [\\"hardworking\\", \\"dedicated\\", \\"thorough\\"]}]"
                    }]
                  }],
                  "generationConfig": {
                    "temperature": 2.0
                  }
                }
                """;

        String response = restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(String.class);

        try {
            JsonNode root = objectMapper.readTree(response);
            String text = root.at("/candidates/0/content/parts/0/text").asText();
            log.info("Gemini ({}) raw text: {}", model, text);
            text = text.replaceAll("(?s)```json\\s*", "").replaceAll("(?s)```\\s*", "").trim();
            log.info("Gemini ({}) parsed text: {}", model, text);
            return objectMapper.readValue(
                    text,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, VocabWord.class)
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse Gemini response: " + e.getMessage(), e);
        }
    }
}
