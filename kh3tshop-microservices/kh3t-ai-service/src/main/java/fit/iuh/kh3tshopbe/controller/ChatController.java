package fit.iuh.kh3tshopbe.controller;

import fit.iuh.kh3tshopbe.service.GeminiService;
import fit.iuh.kh3tshopbe.service.ShopDataService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private static final Map<String, Deque<String>> CHAT_HISTORY = new ConcurrentHashMap<>();
    private static final int MAX_MESSAGES = 10;

    private final GeminiService geminiService;
    private final ShopDataService shopDataService;

    public ChatController(GeminiService geminiService, ShopDataService shopDataService) {
        this.geminiService = geminiService;
        this.shopDataService = shopDataService;
    }

    @PostMapping("/ask")
    public ResponseEntity<Map<String, Object>> askGemini(
            HttpServletRequest request,
            @RequestBody PromptRequest promptRequest
    ) {
        String userPrompt = Optional.ofNullable(promptRequest)
                .map(PromptRequest::getPrompt)
                .orElse("")
                .trim();

        if (userPrompt.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "message", "Dạ anh/chị nhắn gì cho em với ạ!",
                    "suggestedProducts", List.of()
            ));
        }

        String token = extractToken(request.getHeader("Authorization"));
        String userId = token != null ? "user_" + token : "guest";
        Deque<String> history = CHAT_HISTORY.computeIfAbsent(userId, key -> new ArrayDeque<>());

        history.addFirst("user: " + userPrompt);
        keepOnlyLastN(history, MAX_MESSAGES);

        String productContext = buildFullProductContextWithCostPrice();
        String shopInfo = """
            === KH3T SHOP - Trợ lý dễ thương ===
            - Chỉ bán online, ship toàn quốc
            - Đổi trả miễn phí 7 ngày (lỗi NSX)
            - Hotline/Zalo: 0903.456.789
            - Giờ làm: 8h30 - 22h00
            - Quy trình đặt hàng: Chọn sản phẩm, chọn size, nhập thông tin ship hàng, thanh toán qua ngân hàng, ví điện tử, tiền mặt.
            - Các thắc mắc khác liên hệ MrK qua zalo số 0794263939
            """;

        String historyText = history.isEmpty()
                ? ""
                : "Lịch sử chat gần đây (mới nhất ở trên):\n" + String.join("\n", history) + "\n";

        String finalPrompt = """
            Bạn là cô trợ lý mua sắm SIÊU DỄ THƯƠNG của KH3T Shop
            Xưng "em", gọi khách là "anh/chị", dùng thật nhiều emoji
            Trả lời tự nhiên, ngắn gọn, tối đa 3 câu thôi nha!

            Thông tin shop:
            %s

            Lịch sử chat:
            %s

            Danh sách TOÀN BỘ sản phẩm (giá hiển thị là giá bán cuối cùng - costPrice):
            %s

            Khách vừa hỏi: "%s"
            Hãy trả lời thật dễ thương và chính xác nhé!
            """.formatted(shopInfo, historyText, productContext, userPrompt);

        try {
            String reply = geminiService.generateText(finalPrompt);
            String botReply = reply == null || reply.trim().isEmpty()
                    ? "Dạ để em kiểm tra lại giúp anh/chị nha!"
                    : reply.trim();

            history.addFirst("bot: " + botReply);
            keepOnlyLastN(history, MAX_MESSAGES);

            List<Map<String, Object>> suggestedProducts = findSuggestedProducts(botReply);
            List<Long> compareIds = detectCompareRequest(userPrompt, botReply);

            Map<String, Object> response = new HashMap<>();
            response.put("message", botReply);
            response.put("suggestedProducts", suggestedProducts);
            if (compareIds != null && compareIds.size() >= 2 && compareIds.size() <= 4) {
                response.put("compareIds", compareIds);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Em đang hơi lag xíu, anh/chị nhắn lại giúp em nha!");
            errorResponse.put("suggestedProducts", List.of());
            errorResponse.put("compareIds", null);
            return ResponseEntity.ok(errorResponse);
        }
    }

    private List<Map<String, Object>> findSuggestedProducts(String botReply) {
        List<Map<String, Object>> allProducts = shopDataService.getAllProducts();
        if (allProducts.isEmpty()) {
            return List.of();
        }

        String lowerReply = botReply.toLowerCase();
        Set<Long> seenIds = new HashSet<>();
        List<Map<String, Object>> suggestions = new ArrayList<>();

        for (Map<String, Object> product : allProducts) {
            String productName = text(product.get("name")).toLowerCase();
            String normalizedName = productName.replace(" ", "")
                    .replace("-", "")
                    .replace("&", "");

            boolean mentioned = lowerReply.contains(productName)
                    || lowerReply.contains(normalizedName)
                    || lowerReply.contains(productName.replace(" ", ""))
                    || lowerReply.contains(productName.replace("-", ""))
                    || lowerReply.contains(productName.replace("&", ""));

            if (mentioned) {
                Long id = longValue(product.get("id"));
                if (id != null && seenIds.add(id)) {
                    Map<String, Object> suggestion = new HashMap<>();
                    suggestion.put("id", id);
                    suggestion.put("name", text(product.get("name")));
                    suggestions.add(suggestion);
                }
            }

            if (suggestions.size() >= 5) {
                break;
            }
        }

        return suggestions;
    }

    private List<Long> detectCompareRequest(String userPrompt, String botReply) {
        String text = (userPrompt + " " + botReply).toLowerCase();
        boolean isCompareIntent = text.contains("so sánh")
                || text.contains("vs")
                || text.contains("versus")
                || text.contains("đối chiếu")
                || text.contains("khác nhau")
                || text.contains("nên mua cái nào")
                || text.contains("cái nào tốt ho")
                || text.contains("so với")
                || (text.contains("trong 2 cái") && (text.contains("trong hai cái") || text.contains("so kè") || text.contains("phân tích")));

        if (!isCompareIntent) {
            return null;
        }

        List<Map<String, Object>> allProducts = shopDataService.getAllProducts();
        Set<Long> mentionedIds = new HashSet<>();

        for (Map<String, Object> product : allProducts) {
            String name = text(product.get("name")).toLowerCase();
            String cleanName = name.replace(" ", "")
                    .replace("-", "")
                    .replace("&", "")
                    .replace("jeans", "")
                    .replace("shirt", "");

            if (text.contains(name) || text.contains(cleanName)) {
                Long id = longValue(product.get("id"));
                if (id != null) {
                    mentionedIds.add(id);
                }
            }
        }

        if (mentionedIds.size() >= 2 && mentionedIds.size() <= 4) {
            return new ArrayList<>(mentionedIds);
        }

        return null;
    }

    private String buildFullProductContextWithCostPrice() {
        List<Map<String, Object>> products = shopDataService.getAllProducts();
        if (products.isEmpty()) {
            return "Shop đang cập nhật sản phẩm mới ạ!";
        }

        StringBuilder sb = new StringBuilder("=== DANH SÁCH SẢN PHẨM ===\n");

        for (Map<String, Object> product : products) {
            String name = text(product.get("name"));
            String finalPrice = money(numberValue(product.get("costPrice")));
            double discount = numberValue(product.get("discountAmount"));
            double rating = numberValue(product.get("rating"));
            String description = text(product.get("description"));
            String material = text(product.get("material"));
            String form = text(product.get("form"));
            String sizeStr = sizeSummary(product.get("sizeDetails"));

            sb.append(String.format(
                    "• %s | Giá cuối: %s | Giảm: %,.0f%% | Rating: %.1f | Mô tả: %s | Chất liệu: %s | Form: %s | Size còn: %s\n",
                    name,
                    finalPrice,
                    discount,
                    rating,
                    description,
                    material,
                    form,
                    sizeStr
            ));
        }

        sb.append("Tổng cộng: ").append(products.size()).append(" sản phẩm\n");
        return sb.toString();
    }

    private String sizeSummary(Object sizeDetailsValue) {
        if (!(sizeDetailsValue instanceof List<?> sizeDetails) || sizeDetails.isEmpty()) {
            return "Hết hàng";
        }

        List<String> items = new ArrayList<>();
        for (Object sizeDetailObject : sizeDetails) {
            if (!(sizeDetailObject instanceof Map<?, ?> sizeDetail)) {
                continue;
            }

            double quantity = numberValue(sizeDetail.get("quantity"));
            if (quantity <= 0) {
                continue;
            }

            String sizeName = "";
            Object sizeValue = sizeDetail.get("size");
            if (sizeValue instanceof Map<?, ?> sizeMap) {
                sizeName = text(sizeMap.get("nameSize"));
            }

            if (!sizeName.isBlank()) {
                items.add(sizeName + ":" + (int) quantity + "c");
            }
        }

        return items.isEmpty() ? "Hết hàng" : String.join(", ", items);
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

    private Long longValue(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String money(double amount) {
        return String.format("%,.0fđ", amount);
    }

    public static class PromptRequest {
        private String prompt;

        public String getPrompt() {
            return prompt;
        }

        public void setPrompt(String prompt) {
            this.prompt = prompt;
        }
    }
}
