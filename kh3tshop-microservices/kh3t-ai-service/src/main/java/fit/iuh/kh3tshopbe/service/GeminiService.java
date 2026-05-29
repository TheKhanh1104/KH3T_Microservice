package fit.iuh.kh3tshopbe.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    private final WebClient webClient;

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.base-url}")
    private String baseUrl;

    public GeminiService() {
        this.webClient = WebClient.builder().build();
    }

    public String generateText(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            return "Gemini API key chưa được cấu hình.";
        }

        String url = baseUrl + "/models/gemini-2.5-flash:generateContent?key=" + apiKey;
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", prompt)))
                )
        );

        try {
            Map<String, Object> response = webClient.post()
                    .uri(url)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            if (response == null || !response.containsKey("candidates")) {
                return "Gemini API response is empty or invalid.";
            }

            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                return "No candidates returned from Gemini API.";
            }

            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            if (content == null) {
                return "Gemini API response is missing content.";
            }

            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            if (parts == null || parts.isEmpty()) {
                return "Gemini API response is missing parts.";
            }

            Object text = parts.get(0).get("text");
            return text == null ? "" : text.toString();
        } catch (WebClientResponseException e) {
            return "Gemini API error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString();
        } catch (Exception e) {
            return "Error calling Gemini API: " + e.getMessage();
        }
    }
}
