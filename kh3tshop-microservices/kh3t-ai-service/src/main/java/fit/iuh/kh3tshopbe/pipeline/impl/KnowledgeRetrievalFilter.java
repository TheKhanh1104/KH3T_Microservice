package fit.iuh.kh3tshopbe.pipeline.impl;

import fit.iuh.kh3tshopbe.pipeline.AiContext;
import fit.iuh.kh3tshopbe.pipeline.AiFilter;
import fit.iuh.kh3tshopbe.service.ShopDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class KnowledgeRetrievalFilter implements AiFilter {

    private final ShopDataService shopDataService;

    @Override
    public void execute(AiContext context) {
        String prompt = context.getPrompt().toLowerCase();
        
        String shopInfo = """
            === KH3T SHOP - Trợ lý dễ thương ===
            - Chỉ bán online, ship toàn quốc, Hotline/Zalo: 0903.456.789
            - Đổi trả 7 ngày, quy trình đơn giản.
            
            QUY TẮC BẮT BUỘC KHI NHẮC ĐẾN SẢN PHẨM:
            - Khi giới thiệu hoặc nhắc đến bất kỳ sản phẩm nào dưới đây, bạn BẮT BUỘC phải ghi CHÍNH XÁC và ĐẦY ĐỦ tên sản phẩm giống hệt như danh sách cung cấp (Ví dụ: ghi rõ "Hello Kitty | Monogram Laser Baggy Jeans/ Blue" hoặc "Triple Star Small Wallet", KHÔNG ĐƯỢC viết tắt thành "quần Hello Kitty" hay "ví Triple Star").
            - Việc ghi chính xác tên sản phẩm giúp hệ thống nhận diện và hiển thị các thẻ gợi ý (Product Cards) trực quan cho khách hàng bấm vào xem.
            """;
        context.appendSystemContext(shopInfo);

        // Build product context similar to original buildFullProductContextWithCostPrice
        List<Map<String, Object>> products = shopDataService.getAllProducts();
        if (products.isEmpty()) {
            context.appendSystemContext("Shop đang cập nhật sản phẩm mới ạ!");
        } else {
            StringBuilder sb = new StringBuilder("=== DANH SÁCH SẢN PHẨM ===\n");
            for (Map<String, Object> product : products) {
                sb.append(formatProduct(product)).append("\n");
            }
            context.appendSystemContext(sb.toString());
            // Save products to metadata for later filters (suggestions/comparison)
            context.getMetadata().put("allProducts", products);
        }

        // Logic RAG bổ sung: Nếu hỏi về đơn hàng
        if (prompt.contains("đơn hàng") || prompt.contains("lịch sử")) {
            List<Map<String, Object>> orders = shopDataService.getDetailedOrders();
            context.appendSystemContext("Dữ liệu đơn hàng của hệ thống: " + orders.toString());
        }
    }

    private String formatProduct(Map<String, Object> product) {
        String name = text(product.get("name"));
        String finalPrice = money(numberValue(product.get("costPrice")));
        double discount = numberValue(product.get("discountAmount"));
        double rating = numberValue(product.get("rating"));
        String description = text(product.get("description"));
        
        return String.format("• %s | Giá: %s | Giảm: %,.0f%% | Rating: %.1f | Mô tả: %s", 
                name, finalPrice, discount, rating, description);
    }

    private String text(Object value) { return value == null ? "" : value.toString(); }
    private double numberValue(Object value) {
        if (value instanceof Number n) return n.doubleValue();
        try { return Double.parseDouble(value.toString()); } catch (Exception e) { return 0; }
    }
    private String money(double amount) { return String.format("%,.0fđ", amount); }

    @Override
    public int getOrder() {
        return 2;
    }
}
