package fit.iuh.kh3tshopbe.controller;

import fit.iuh.kh3tshopbe.service.GeminiService;
import fit.iuh.kh3tshopbe.service.ShopDataService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/admin-chat")
public class AdminChatController {

    private static final Map<String, Deque<String>> CHAT_HISTORY = new ConcurrentHashMap<>();
    private static final int MAX_MESSAGES = 10;

    private final GeminiService geminiService;
    private final ShopDataService shopDataService;

    public AdminChatController(GeminiService geminiService, ShopDataService shopDataService) {
        this.geminiService = geminiService;
        this.shopDataService = shopDataService;
    }

    @PostMapping("/ask")
    public ResponseEntity<String> askAdminBot(
            HttpServletRequest request,
            @RequestBody Map<String, String> body
    ) {
        String prompt = body.get("prompt");
        if (prompt == null || prompt.trim().isEmpty()) {
            return ResponseEntity.ok("Sếp nhắn gì cho em với ạ!");
        }
        prompt = prompt.trim();

        String token = extractToken(request.getHeader("Authorization"));
        String adminId;

        if (token != null && !token.isEmpty()) {
            adminId = "admin_" + token.hashCode();
        } else {
            String guestAdminId = (String) request.getSession().getAttribute("adminGuestChatId");
            if (guestAdminId == null) {
                guestAdminId = "admin_guest_" + UUID.randomUUID().toString().substring(0, 8);
                request.getSession().setAttribute("adminGuestChatId", guestAdminId);
            }
            adminId = guestAdminId;
        }

        Deque<String> history = CHAT_HISTORY.computeIfAbsent(adminId, key -> new ArrayDeque<>());
        history.addFirst("admin: " + prompt);
        keepOnlyLastN(history, MAX_MESSAGES);

        String stats = generateAdminStats();
        String historyText = history.isEmpty()
                ? ""
                : "Lịch sử chat gần đây (mới nhất ở trên):\n" + String.join("\n", history) + "\n";

        String finalPrompt = """
            Bạn là Trợ lý CEO siêu thông minh của KH3T Shop – cực kỳ chuyên nghiệp, nói chuyện như giám đốc!
            Xưng "em", gọi admin là "sếp" hoặc "anh/chị chủ shop"
            Dùng tiếng Việt, trả lời ngắn gọn, có số liệu chính xác, thêm biểu tượng cảm xúc phù hợp
            Không cần chào hỏi dài dòng, đi thẳng vào vấn đề luôn!

            DỮ LIỆU THỐNG KÊ HIỆN TẠI (cập nhật realtime):
            %s

            Lịch sử chat:
            %s

            Sếp hỏi: "%s"
            Hãy trả lời cực kỳ chuyên nghiệp, có số liệu, có dự đoán nếu được!
            """.formatted(stats, historyText, prompt);

        try {
            String reply = geminiService.generateText(finalPrompt);
            String botReply = reply == null || reply.trim().isEmpty()
                    ? "Em đang tính toán giúp sếp..."
                    : reply.trim();

            history.addFirst("bot: " + botReply);
            keepOnlyLastN(history, MAX_MESSAGES);

            return ResponseEntity.ok(botReply);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok("Em đang cập nhật dữ liệu, sếp hỏi lại sau 1 phút nha!");
        }
    }

    private String generateAdminStats() {
        LocalDate today = LocalDate.now();
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate sevenDaysAgo = today.minusDays(7);

        List<Map<String, Object>> detailedOrders = shopDataService.getDetailedOrders();
        double revenueToday = 0;
        double revenueLast7Days = 0;
        Map<LocalDate, Double> revenueByDay = new HashMap<>();
        long pendingOrders = 0;
        long cancelledOrders = 0;

        for (Map<String, Object> order : detailedOrders) {
            LocalDate orderDate = parseDate(order.get("date"));
            double total = numberValue(order.get("total"));
            String status = text(order.get("status"));

            if (orderDate != null) {
                if (orderDate.isEqual(today)) {
                    revenueToday += total;
                }
                if (!orderDate.isBefore(sevenDaysAgo) && !orderDate.isAfter(today)) {
                    revenueLast7Days += total;
                }
                if (!orderDate.isBefore(startOfMonth)) {
                    revenueByDay.put(orderDate, revenueByDay.getOrDefault(orderDate, 0.0) + total);
                }
            }

            if ("PENDING".equalsIgnoreCase(status)) {
                pendingOrders++;
            }
            if ("CANCELLED".equalsIgnoreCase(status)) {
                cancelledOrders++;
            }
        }

        LocalDate highestRevenueDay = revenueByDay.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
        double highestRevenueAmount = highestRevenueDay != null ? revenueByDay.get(highestRevenueDay) : 0;

        List<String> top5Products = shopDataService.getTopTrendingProducts("week").stream()
                .sorted(Comparator.comparingDouble((Map<String, Object> item) -> numberValue(item.get("sales"))).reversed())
                .limit(5)
                .map(item -> text(item.get("name")))
                .filter(name -> !name.isBlank())
                .toList();

        return """
Doanh thu hôm nay: %,.0fđ
Doanh thu 7 ngày qua: %,.0fđ
Ngày doanh thu cao nhất tháng: %s (%,.0fđ)
Top 5 sản phẩm bán chạy:
%s
Đơn chờ xử lý: %d
Đơn bị hủy: %d
""".formatted(
                revenueToday,
                revenueLast7Days,
                highestRevenueDay != null ? highestRevenueDay : "Chưa có",
                highestRevenueAmount,
                String.join(", ", top5Products),
                pendingOrders,
                cancelledOrders
        );
    }

    private void keepOnlyLastN(Deque<String> deque, int max) {
        while (deque.size() > max) {
            deque.removeLast();
        }
    }

    private String extractToken(String header) {
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }
        return header.substring(7).trim();
    }

    private LocalDate parseDate(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Number number) {
            return java.time.Instant.ofEpochMilli(number.longValue())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }

        String text = value.toString().trim();
        if (text.isEmpty()) {
            return null;
        }

        try {
            return java.time.Instant.parse(text)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        } catch (DateTimeParseException ignored) {
        }

        try {
            return LocalDateTime.parse(text).toLocalDate();
        } catch (DateTimeParseException ignored) {
        }

        try {
            return LocalDateTime.parse(text.replace(" ", "T")).toLocalDate();
        } catch (DateTimeParseException ignored) {
        }

        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException ignored) {
        }

        return null;
    }

    private String text(Object value) {
        return value == null ? "" : value.toString();
    }

    private double numberValue(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value == null) {
            return 0;
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
