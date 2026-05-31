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
@RequestMapping("/admin-chat")
public class AdminChatController {

    private static final Map<String, Deque<String>> CHAT_HISTORY = new ConcurrentHashMap<>();
    private static final int MAX_MESSAGES = 10;

    private final AiPipelineManager aiPipelineManager;

    public AdminChatController(AiPipelineManager aiPipelineManager) {
        this.aiPipelineManager = aiPipelineManager;
    }

    @PostMapping("/ask")
    public ResponseEntity<String> askAdminBot(
            HttpServletRequest request,
            @RequestBody Map<String, String> body
    ) {
        try {
            String prompt = body.get("prompt");
            if (prompt == null || prompt.trim().isEmpty()) {
                return ResponseEntity.ok("Sếp nhắn gì cho em với ạ!");
            }

            String userId = getAdminIdFromRequest(request);
            Deque<String> history = CHAT_HISTORY.computeIfAbsent(userId, k -> new ConcurrentLinkedDeque<>());

            // Chuẩn bị metadata cho Pipeline
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("role", "ADMIN");
            metadata.put("history", new ArrayList<>(history));

            // THỰC THI PIPELINE
            AiContext context = aiPipelineManager.execute(prompt, metadata);

            if (context.getErrorMessage() != null) {
                return ResponseEntity.ok("Em đang gặp chút sự cố, sếp hỏi lại sau nhé!");
            }

            String botReply = context.getFinalResponse();
            
            // Lưu lịch sử (Dạng String đơn giản như code cũ của Admin)
            history.addFirst("admin: " + prompt);
            history.addFirst("bot: " + botReply);
            while (history.size() > MAX_MESSAGES) history.removeLast();

            return ResponseEntity.ok(botReply);

        } catch (Exception e) {
            return ResponseEntity.ok("Em đang cập nhật dữ liệu, sếp hỏi lại sau 1 phút nha!");
        }
    }

    private String getAdminIdFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return "admin_" + header.hashCode();
        }
        String guestAdminId = (String) request.getSession().getAttribute("adminGuestChatId");
        if (guestAdminId == null) {
            guestAdminId = "admin_guest_" + UUID.randomUUID().toString().substring(0, 8);
            request.getSession().setAttribute("adminGuestChatId", guestAdminId);
        }
        return guestAdminId;
    }
}
