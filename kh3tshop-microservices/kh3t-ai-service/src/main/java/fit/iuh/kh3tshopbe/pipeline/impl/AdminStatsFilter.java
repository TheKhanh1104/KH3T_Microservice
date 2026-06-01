package fit.iuh.kh3tshopbe.pipeline.impl;

import fit.iuh.kh3tshopbe.pipeline.AiContext;
import fit.iuh.kh3tshopbe.pipeline.AiFilter;
import fit.iuh.kh3tshopbe.service.ShopDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.*;

@Component
@RequiredArgsConstructor
public class AdminStatsFilter implements AiFilter {

    private final ShopDataService shopDataService;

    @Override
    public void execute(AiContext context) {
        String role = (String) context.getMetadata().get("role");
        if (!"ADMIN".equals(role)) return;

        context.appendSystemContext("Bạn là Trợ lý CEO của KH3T Shop. Hãy trả lời cực kỳ chuyên nghiệp, có số liệu.");

        // Reusing the logic from your AdminChatController
        String stats = generateAdminStats();
        context.appendSystemContext("DỮ LIỆU THỐNG KÊ HIỆN TẠI:\n" + stats);
    }

    private String generateAdminStats() {
        List<Map<String, Object>> detailedOrders = shopDataService.getDetailedOrders();

        // Dynamically find the latest order date to use as the reporting anchor ("today")
        LocalDate today = null;
        for (Map<String, Object> order : detailedOrders) {
            LocalDate orderDate = parseDate(order.get("date"));
            if (orderDate != null) {
                if (today == null || orderDate.isAfter(today)) {
                    today = orderDate;
                }
            }
        }
        if (today == null) {
            today = LocalDate.now();
        }

        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate sevenDaysAgo = today.minusDays(7);

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
                if (orderDate.isEqual(today)) revenueToday += total;
                if (!orderDate.isBefore(sevenDaysAgo) && !orderDate.isAfter(today)) revenueLast7Days += total;
                if (!orderDate.isBefore(startOfMonth)) {
                    revenueByDay.put(orderDate, revenueByDay.getOrDefault(orderDate, 0.0) + total);
                }
            }
            if ("PENDING".equalsIgnoreCase(status)) pendingOrders++;
            if ("CANCELLED".equalsIgnoreCase(status)) cancelledOrders++;
        }

        LocalDate highestRevenueDay = revenueByDay.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse(null);
        double highestRevenueAmount = highestRevenueDay != null ? revenueByDay.get(highestRevenueDay) : 0;

        List<String> top5Products = shopDataService.getTopTrendingProducts("week").stream()
                .sorted(Comparator.comparingDouble((Map<String, Object> item) -> numberValue(item.get("sales"))).reversed())
                .limit(5)
                .map(item -> text(item.get("name")))
                .filter(name -> !name.isBlank()).toList();

        return String.format("""
Doanh thu hôm nay: %,.0fđ
Doanh thu 7 ngày qua: %,.0fđ
Ngày doanh thu cao nhất tháng: %s (%,.0fđ)
Top 5 sản phẩm bán chạy: %s
Đơn chờ xử lý: %d
Đơn bị hủy: %d
""", revenueToday, revenueLast7Days, highestRevenueDay != null ? highestRevenueDay : "Chưa có", 
highestRevenueAmount, String.join(", ", top5Products), pendingOrders, cancelledOrders);
    }

    private LocalDate parseDate(Object v) {
        if (v == null) return null;
        if (v instanceof Number n) return java.time.Instant.ofEpochMilli(n.longValue()).atZone(ZoneId.systemDefault()).toLocalDate();
        String t = v.toString().trim();
        if (t.isEmpty()) return null;
        try { return java.time.Instant.parse(t).atZone(ZoneId.systemDefault()).toLocalDate(); } catch (Exception ignored) {}
        try { return LocalDateTime.parse(t).toLocalDate(); } catch (Exception ignored) {}
        try { return LocalDate.parse(t); } catch (Exception ignored) {}
        return null;
    }

    private String text(Object v) { return v == null ? "" : v.toString(); }
    private double numberValue(Object v) {
        if (v instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(v.toString()); } catch (Exception e) { return 0; }
    }

    @Override
    public int getOrder() {
        return 2; // Cùng cấp với KnowledgeRetrievalFilter nhưng xử lý cho ADMIN
    }
}
