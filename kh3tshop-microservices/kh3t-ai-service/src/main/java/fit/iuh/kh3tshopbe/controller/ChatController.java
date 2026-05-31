package fit.iuh.kh3tshopbe.controller;

import fit.iuh.kh3tshopbe.pipeline.AiContext;
import fit.iuh.kh3tshopbe.pipeline.AiPipelineManager;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private static final Map<String, Deque<Map<String, Object>>> CHAT_HISTORY = new ConcurrentHashMap<>();
    private static final int MAX_MESSAGES = 10;

    private final AiPipelineManager aiPipelineManager;

    public ChatController(AiPipelineManager aiPipelineManager) {
        this.aiPipelineManager = aiPipelineManager;
    }

    @GetMapping("/history")
    public ResponseEntity<List<Map<String, Object>>> getChatHistory(HttpServletRequest request) {
        String userId = getUserIdFromRequest(request);
        Deque<Map<String, Object>> history = CHAT_HISTORY.get(userId);
        return ResponseEntity.ok(history == null ? List.of() : new ArrayList<>(history));
    }

    @PostMapping("/ask")
    public ResponseEntity<Map<String, Object>> askGemini(
            HttpServletRequest request,
            @RequestBody PromptRequest promptRequest
    ) {
        try {
            String userPrompt = Optional.ofNullable(promptRequest).map(PromptRequest::getPrompt).orElse("").trim();
            String userId = getUserIdFromRequest(request);
            Deque<Map<String, Object>> history = CHAT_HISTORY.computeIfAbsent(userId, k -> new ConcurrentLinkedDeque<>());

            // Chuẩn bị metadata cho Pipeline
            Map<String, Object> initialMetadata = new HashMap<>();
            initialMetadata.put("history", new ArrayList<>(history));
            initialMetadata.put("userId", userId);

            // THỰC THI PIPELINE
            AiContext context = aiPipelineManager.execute(userPrompt, initialMetadata);

            if (context.getErrorMessage() != null) {
                return ResponseEntity.internalServerError().body(Map.of("text", context.getErrorMessage()));
            }

            String botReply = context.getFinalResponse();
            List<Map<String, Object>> suggestedProducts = (List<Map<String, Object>>) context.getMetadata().getOrDefault("suggestedProducts", List.of());
            List<Long> compareIds = (List<Long>) context.getMetadata().get("compareIds");

            // Lưu lịch sử
            saveToHistory(history, userPrompt, botReply, suggestedProducts, compareIds);

            // Trả về kết quả
            Map<String, Object> response = new HashMap<>();
            response.put("sender", "bot");
            response.put("text", botReply);
            response.put("suggestedProducts", suggestedProducts);
            if (compareIds != null) response.put("compareIds", compareIds);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("text", "Dạ em hơi lag xíu, anh nhắn lại giúp em nhé! 🌸"));
        }
    }

    private void saveToHistory(Deque<Map<String, Object>> history, String userPrompt, String botReply, 
                               List<Map<String, Object>> suggestedProducts, List<Long> compareIds) {
        Map<String, Object> userMsg = Map.of("sender", "user", "text", userPrompt);
        history.addLast(userMsg);

        Map<String, Object> botMsg = new HashMap<>();
        botMsg.put("sender", "bot");
        botMsg.put("text", botReply);
        botMsg.put("suggestedProducts", suggestedProducts);
        if (compareIds != null) botMsg.put("compareIds", compareIds);
        history.addLast(botMsg);

        while (history.size() > MAX_MESSAGES * 2) history.pollFirst();
    }

    private String getUserIdFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) return "guest";
        try {
            String token = header.substring(7);
            String payload = new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]));
            Map<String, Object> body = new ObjectMapper().readValue(payload, Map.class);
            return body.getOrDefault("sub", body.getOrDefault("username", "user_unknown")).toString();
        } catch (Exception e) {
            return "guest_fallback";
        }
    }

    public static class PromptRequest {
        private String prompt;
        public String getPrompt() { return prompt; }
        public void setPrompt(String prompt) { this.prompt = prompt; }
    }
}
