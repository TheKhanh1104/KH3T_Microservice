package fit.iuh.kh3tshopbe.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fit.iuh.kh3tshopbe.entities.Product;
import fit.iuh.kh3tshopbe.repository.ProductRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ProductCacheService {

    private final ProductRepository productRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper; // Để chuyển đổi JSON

    private static final String CACHE_KEY = "allProducts";
    private static final long CACHE_TTL = 10; // 10 phút

    public ProductCacheService(ProductRepository productRepository, RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.productRepository = productRepository;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public List<Product> getAllProducts() {
        // Thử lấy từ cache trước
        Object cachedData = redisTemplate.opsForValue().get(CACHE_KEY);
        if (cachedData != null) {
            // Chuyển đổi từ object (thường là LinkedHashMap) sang List<Product>
            try {
                return objectMapper.convertValue(cachedData, new TypeReference<List<Product>>() {});
            } catch (Exception e) {
                System.err.println("Error converting cached data: " + e.getMessage());
                // Nếu có lỗi, xóa cache và tải lại
                redisTemplate.delete(CACHE_KEY);
            }
        }

        // Nếu cache không có hoặc lỗi, tải lại từ DB
        return refreshCache();
    }

    public synchronized List<Product> refreshCache() {
        List<Product> products = productRepository.findAllWithDetails(); // JOIN FETCH
        // Lưu vào Redis với thời gian sống (TTL)
        redisTemplate.opsForValue().set(CACHE_KEY, products, CACHE_TTL, TimeUnit.MINUTES);
        return products;
    }

    public void evictProductsCache() {
        redisTemplate.delete(CACHE_KEY);
    }
}