package fit.iuh.kh3tshopbe.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class ShopDataService {

    private final WebClient webClient = WebClient.builder().build();

    @Value("${services.catalog.url}")
    private String catalogBaseUrl;

    @Value("${services.order.url}")
    private String orderBaseUrl;

    public List<Map<String, Object>> getAllProducts() {
        return unwrapResult(fetchMap(catalogBaseUrl + "/products"));
    }

    public List<Map<String, Object>> getTopTrendingProducts(String type) {
        List<Map<String, Object>> items = fetchList(catalogBaseUrl + "/products/top-trending?type=" + type);
        return items == null ? Collections.emptyList() : items;
    }

    public List<Map<String, Object>> getDetailedOrders() {
        List<Map<String, Object>> items = fetchList(orderBaseUrl + "/orders/detailed-orders");
        return items == null ? Collections.emptyList() : items;
    }

    private Map<String, Object> fetchMap(String url) {
        try {
            Map<String, Object> response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();
            return response == null ? Collections.emptyMap() : response;
        } catch (Exception e) {
            System.err.println("Failed to fetch map from " + url + ": " + e.getMessage());
            return Collections.emptyMap();
        }
    }

    private List<Map<String, Object>> fetchList(String url) {
        try {
            List<Map<String, Object>> response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                    .block();
            return response == null ? Collections.emptyList() : response;
        } catch (Exception e) {
            System.err.println("Failed to fetch list from " + url + ": " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<Map<String, Object>> unwrapResult(Map<String, Object> response) {
        Object result = response.get("result");
        if (result instanceof List<?> list) {
            return list.stream()
                    .filter(Map.class::isInstance)
                    .map(item -> (Map<String, Object>) item)
                    .toList();
        }
        return Collections.emptyList();
    }
}
