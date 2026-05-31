package fit.iuh.kh3tshopbe.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    private static final List<String> FALLBACK_MODELS = List.of(
            "openrouter/free",
            "meta-llama/llama-3.2-3b-instruct:free",
            "meta-llama/llama-3.1-8b-instruct:free",
            "qwen/qwen-2.5-7b-instruct:free",
            "google/gemma-2-9b-it:free"
    );

    private final WebClient.Builder webClientBuilder;
    private WebClient webClient;

    @Value("${gemini.api.key:}")
    private String apiKey;

    @Value("${gemini.base-url:https://openrouter.ai/api/v1}")
    private String baseUrl;

    public GeminiService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    @PostConstruct
    public void init() {
        String finalUrl = baseUrl;
        if (finalUrl == null || finalUrl.isBlank()) {
            finalUrl = "https://openrouter.ai/api/v1/";
        }
        if (!finalUrl.endsWith("/")) {
            finalUrl += "/";
        }
        this.webClient = webClientBuilder.baseUrl(finalUrl).build();
        System.out.println("=== AI Service BASE URL: " + finalUrl + " ===");
    }

    public String generateRawText(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            apiKey = System.getenv("GEMINI_API_KEY");
        }

        if (apiKey == null || apiKey.isBlank()) {
            return "HỆ THỐNG: API Key chưa được nạp. Vui lòng kiểm tra môi trường hoặc cấu hình GEMINI_API_KEY trên Render.";
        }

        StringBuilder errorLog = new StringBuilder();
        for (String model : FALLBACK_MODELS) {
            try {
                String result = callModel(model, prompt);
                if (result != null && !result.isBlank()) {
                    System.out.println("=== Used model: " + model + " ===");
                    return result;
                }
            } catch (WebClientResponseException e) {
                int status = e.getStatusCode().value();
                String body = e.getResponseBodyAsString();
                String errMsg = String.format("Model %s failed: HTTP %d - %s", model, status, body);
                System.err.println("=== " + errMsg + " ===");
                errorLog.append("- ").append(errMsg).append("\n");
            } catch (Exception e) {
                String errMsg = String.format("Model %s error: %s", model, e.getMessage());
                System.err.println("=== " + errMsg + " ===");
                errorLog.append("- ").append(errMsg).append("\n");
            }
        }
        
        // Thay vì trả về null làm hiển thị "Dạ em chưa hiểu lắm ạ!", trả về thông tin lỗi chi tiết để dev/user dễ chẩn đoán
        return "Dạ hệ thống AI đang gặp lỗi kết nối đến OpenRouter. Chi tiết kỹ thuật:\n" + errorLog.toString();
    }

    public String generateText(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return "Em chưa nhận được câu hỏi, anh/chị vui lòng gửi lại nhé!";
        }
        String res = generateRawText(prompt);
        return res != null ? res : "Dạ hệ thống đang bận, anh/chị vui lòng nhắn lại sau ít phút nhé! 🙏";
    }

    private String callModel(String model, String prompt) {
        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                )
        );

        Map<String, Object> response = webClient.post()
                .uri("chat/completions") // Sử dụng relative URI để tránh bị WebClient loại bỏ '/api/v1' của baseUrl
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .header("HTTP-Referer", "http://localhost:8080")
                .header("X-Title", "KH3T Shop")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();

        if (response == null || !response.containsKey("choices")) return null;

        List<?> choices = (List<?>) response.get("choices");
        if (choices == null || choices.isEmpty()) return null;

        Map<?, ?> firstChoice = (Map<?, ?>) choices.get(0);
        if (firstChoice == null) return null;

        Map<?, ?> message = (Map<?, ?>) firstChoice.get("message");
        if (message == null) return null;

        Object content = message.get("content");
        return content == null ? null : content.toString().trim();
    }
}