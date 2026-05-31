package fit.iuh.kh3tshopbe.service;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    private static final List<String> GOOGLE_SCOPES = List.of("https://www.googleapis.com/auth/cloud-platform");

    @Value("${gemini.api.key:}")
    private String apiKey;

    private final WebClient webClient;

    public GeminiService(WebClient webClient) {
        this.webClient = webClient;
    }

    public String generateText(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return "Em chưa nhận được câu hỏi, anh/chị vui lòng gửi lại nhé!";
        }

        if (apiKey == null || apiKey.isBlank()) {
            apiKey = System.getenv("GEMINI_API_KEY");
        }

        String bearerToken = resolveBearerToken();
        boolean useApiKey = false;

        if (bearerToken == null) {
            if (isValidApiKey(apiKey)) {
                useApiKey = true;
            } else {
                return "THÔNG BÁO: Gemini credential chưa được nạp hoặc không hợp lệ. Vui lòng kiểm tra GEMINI_API_KEY/GEMINI_ACCESS_TOKEN hoặc Google Application Default Credentials.";
            }
        }

        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", prompt)))
                )
        );

        try {
            WebClient.RequestBodySpec request = webClient.post().uri(useApiKey ? url + "?key=" + apiKey : url)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(requestBody);

            if (!useApiKey) {
                request = request.header(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken);
            }

            Map<String, Object> response = request.retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();

            if (response == null || !response.containsKey("candidates")) {
                return "Lỗi: API trả về kết quả rỗng.";
            }
            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            if (candidates.isEmpty()) {
                return "Lỗi: Không tìm thấy phản hồi từ AI.";
            }
            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            return parts.isEmpty() ? "Lỗi: Phản hồi Gemini không chứa nội dung." : (String) parts.get(0).get("text");

        } catch (WebClientResponseException e) {
            return "Lỗi Gemini (HTTP " + e.getStatusCode() + "): " + e.getResponseBodyAsString();
        } catch (Exception e) {
            return "Lỗi kết nối AI: " + e.getMessage();
        }
    }

    private String resolveBearerToken() {
        String token = System.getenv("GEMINI_ACCESS_TOKEN");
        if (isBearerToken(token)) {
            return token;
        }
        if (!isValidApiKey(apiKey) && isBearerToken(apiKey)) {
            return apiKey;
        }

        try {
            GoogleCredentials credentials = loadGoogleCredentials();
            if (credentials == null) {
                return null;
            }
            credentials = credentials.createScoped(GOOGLE_SCOPES);
            credentials.refreshIfExpired();
            AccessToken accessToken = credentials.getAccessToken();
            return accessToken == null ? null : accessToken.getTokenValue();
        } catch (Exception e) {
            return null;
        }
    }

    private GoogleCredentials loadGoogleCredentials() {
        try {
            String credsPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
            if (credsPath != null && !credsPath.isBlank() && Files.exists(Path.of(credsPath))) {
                return GoogleCredentials.fromStream(new FileInputStream(credsPath));
            }
            return GoogleCredentials.getApplicationDefault();
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isValidApiKey(String key) {
        return key != null && !key.isBlank() && (key.startsWith("AIza") || key.startsWith("AIzaSy"));
    }

    private boolean isBearerToken(String token) {
        return token != null && !token.isBlank() && (token.startsWith("ya29.") || token.startsWith("AQ.") || token.contains("."));
    }
}
