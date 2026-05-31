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
            "google/gemma-2-9b-it:free",
            "meta-llama/llama-3-8b-instruct:free",
            "deepseek/deepseek-r1:free",
            "qwen/qwen-2-7b-instruct:free",
            "microsoft/phi-3-medium-128k-instruct:free"
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
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        System.out.println("=== AI Service BASE URL: " + baseUrl + " ===");
    }

    public String generateRawText(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            apiKey = System.getenv("GEMINI_API_KEY");
        }

        if (apiKey == null || apiKey.isBlank()) {
            return "HỆ THỐNG: API Key chưa được nạp. Vui lòng kiểm tra môi trường hoặc file .env.";
        }

        for (String model : FALLBACK_MODELS) {
            try {
                String result = callModel(model, prompt);
                if (result != null && !result.isBlank()) {
                    System.out.println("=== Used model: " + model + " ===");
                    return result;
                }
            } catch (WebClientResponseException e) {
                int status = e.getStatusCode().value();
                System.out.println("=== Model " + model + " failed with " + status + ", trying next... ===");
                // Không ném exception (throw e) để tiếp tục thử các model fallback khác nếu model này bị lỗi (như lỗi 400 do sai tên model hoặc hết quota)
            } catch (Exception e) {
                System.out.println("=== Model " + model + " error: " + e.getMessage() + ", trying next... ===");
            }
        }
        return null;
    }

    public String generateText(String prompt) {
        // Giữ lại phương thức này để không làm hỏng các code cũ nếu có, 
        // nhưng bên trong sẽ gọi generateRawText
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
                .uri("/chat/completions")
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