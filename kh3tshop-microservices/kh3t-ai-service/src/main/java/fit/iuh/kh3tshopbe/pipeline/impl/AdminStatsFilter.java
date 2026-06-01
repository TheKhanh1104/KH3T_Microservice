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

        context.appendSystemContext(
            "Bạn là Trợ lý CEO của KH3T Shop. Hãy trả lời cực kỳ chuyên nghiệp, có số liệu.\n" +
            "Khi hiển thị danh sách sản phẩm, danh sách hàng hết/sắp hết, danh sách đơn hàng hoặc dữ liệu nhân viên, bạn BẮT BUỘC phải dùng định dạng BẢNG MARKDOWN (Markdown Table) (ví dụ: | STT | ID | Tên sản phẩm | Tồn kho | Giá bán | ... |) để người dùng dễ quan sát trực quan."
        );

        // Append general revenue/order statistics
        String stats = generateAdminStats();
        context.appendSystemContext("DỮ LIỆU THỐNG KÊ DOANH THU & ĐƠN HÀNG CHUNG:\n" + stats);

        // Append detailed product inventory/stock
        String stockInfo = getProductStockInfo();
        context.appendSystemContext(stockInfo);

        // Append detailed staff/employee info
        String staffInfo = getStaffInfo();
        context.appendSystemContext(staffInfo);

        // Append detailed individual orders
        String ordersInfo = getDetailedOrdersInfo();
        context.appendSystemContext(ordersInfo);
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

    private String money(double amount) { return String.format("%,.0fđ", amount); }

    private String getProductStockInfo() {
        try {
            List<Map<String, Object>> products = shopDataService.getAllProducts();
            StringBuilder sb = new StringBuilder("\nDỮ LIỆU KHO HÀNG & TỒN KHO SẢN PHẨM HỆ THỐNG:\n");
            sb.append("| ID | Tên sản phẩm | Tồn kho | Giá bán | Form | Chất liệu |\n");
            sb.append("|---|---|---|---|---|---|\n");
            for (Map<String, Object> p : products) {
                sb.append(String.format("| %s | %s | %s | %s | %s | %s |\n",
                    text(p.get("id")), text(p.get("name")), text(p.get("quantity")), money(numberValue(p.get("price"))),
                    text(p.get("form")), text(p.get("material"))));
            }
            return sb.toString();
        } catch (Exception e) {
            return "\n[Lỗi tải dữ liệu kho hàng: " + e.getMessage() + "]\n";
        }
    }

    private String getStaffInfo() {
        try {
            List<Map<String, Object>> accounts = shopDataService.getAllAccounts();
            StringBuilder sb = new StringBuilder("\nDỮ LIỆU NHÂN VIÊN & ACCOUNT STAFF HỆ THỐNG:\n");
            sb.append("| Tên nhân viên | Username | Email | SĐT | Trạng thái |\n");
            sb.append("|---|---|---|---|---|\n");
            for (Map<String, Object> acc : accounts) {
                String roleName = text(acc.get("role"));
                if ("STAFF".equals(roleName)) {
                    Map<String, Object> cust = (Map<String, Object>) acc.get("customer");
                    String fullName = cust != null ? text(cust.get("fullName")) : "N/A";
                    String email = cust != null ? text(cust.get("email")) : "N/A";
                    String phone = cust != null ? text(cust.get("phoneNumber")) : "N/A";
                    String status = text(acc.get("statusLogin"));
                    sb.append(String.format("| %s | %s | %s | %s | %s |\n",
                        fullName, text(acc.get("username")), email, phone, status));
                }
            }
            return sb.toString();
        } catch (Exception e) {
            return "\n[Lỗi tải danh sách nhân viên: " + e.getMessage() + "]\n";
        }
    }

    private String getDetailedOrdersInfo() {
        try {
            List<Map<String, Object>> detailedOrders = shopDataService.getDetailedOrders();
            StringBuilder sb = new StringBuilder("\nDANH SÁCH ĐƠN HÀNG CHI TIẾT ĐỂ BÁO CÁO:\n");
            sb.append("| Mã Đơn | Tên Khách Hàng | Tổng tiền | PTTT | Trạng thái | Ngày đặt | Số lượng SP |\n");
            sb.append("|---|---|---|---|---|---|---|\n");
            for (Map<String, Object> order : detailedOrders) {
                sb.append(String.format("| %s | %s | %s | %s | %s | %s | %s |\n",
                    text(order.get("id")), text(order.get("customer")), money(numberValue(order.get("total"))),
                    text(order.get("payment")), text(order.get("status")), text(order.get("date")), text(order.get("items"))));
            }
            return sb.toString();
        } catch (Exception e) {
            return "\n[Lỗi tải danh sách đơn hàng: " + e.getMessage() + "]\n";
        }
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
