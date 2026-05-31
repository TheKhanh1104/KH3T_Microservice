package fit.iuh.kh3tshopbe.pipeline.impl;

import fit.iuh.kh3tshopbe.pipeline.AiContext;
import fit.iuh.kh3tshopbe.pipeline.AiFilter;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class PostProcessingFilter implements AiFilter {

    @Override
    public void execute(AiContext context) {
        String botReply = context.getRawResponse();
        if (botReply == null) return;

        List<Map<String, Object>> allProducts = (List<Map<String, Object>>) context.getMetadata().get("allProducts");
        if (allProducts == null) return;

        // Suggested Products Logic
        List<Map<String, Object>> suggestions = findSuggestedProducts(botReply, allProducts);
        context.getMetadata().put("suggestedProducts", suggestions);

        // Comparison Detection Logic
        List<Long> compareIds = detectCompareRequest(context.getPrompt(), botReply, allProducts);
        if (compareIds != null && !compareIds.isEmpty()) {
            context.getMetadata().put("compareIds", compareIds);
        }
    }

    private List<Map<String, Object>> findSuggestedProducts(String botReply, List<Map<String, Object>> allProducts) {
        String lowerReply = botReply.toLowerCase();
        Set<Long> seenIds = new HashSet<>();
        List<Map<String, Object>> suggestions = new ArrayList<>();

        for (Map<String, Object> product : allProducts) {
            String productName = text(product.get("name")).toLowerCase();
            if (productName.isEmpty()) continue;
            
            String normalizedName = productName.replace(" ", "").replace("-", "").replace("&", "");

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
            if (suggestions.size() >= 5) break;
        }
        return suggestions;
    }

    private List<Long> detectCompareRequest(String userPrompt, String botReply, List<Map<String, Object>> allProducts) {
        String text = (userPrompt + " " + botReply).toLowerCase();
        boolean isCompareIntent = text.contains("so sánh") || text.contains(" vs ") || text.contains("khác nhau")
                || text.contains("nên mua cái nào") || text.contains("giữa") || text.contains("với");

        if (!isCompareIntent) return null;

        Set<Long> mentionedIds = new HashSet<>();
        for (Map<String, Object> product : allProducts) {
            String name = text(product.get("name")).toLowerCase();
            if (name.length() < 3) continue;

            if (text.contains(name)) {
                Long id = longValue(product.get("id"));
                if (id != null) mentionedIds.add(id);
            }
        }

        if (mentionedIds.size() >= 2 && mentionedIds.size() <= 4) {
            return new ArrayList<>(mentionedIds);
        }
        return null;
    }

    private String text(Object v) { return v == null ? "" : v.toString(); }
    private Long longValue(Object v) {
        if (v instanceof Number n) return n.longValue();
        try { return Long.parseLong(v.toString()); } catch (Exception e) { return null; }
    }

    @Override
    public int getOrder() {
        return 5;
    }
}
