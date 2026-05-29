package fit.iuh.kh3tshopbe.product.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fit.iuh.kh3tshopbe.product.repository.ProductRepository;
import fit.iuh.kh3tshopbe.shared.entity.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * [MODULE: product / CACHE-ASIDE PATTERN]
 *
 * Moved từ service.ProductCacheService sang product.cache.ProductCacheService.
 * Thuộc module product, chỉ phụ trách cache cho Product.
 *
 * Note: evictProductsCache() không còn được gọi trực tiếp từ CommandService.
 * Thay vào đó, nó được gọi bởi ProductEventListener (Observer Pattern).
 */
@Service
@Slf4j
public class ProductCacheService {

    private final ProductRepository productRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String CACHE_KEY = "allProducts";
    private static final long CACHE_TTL = 10; // 10 phút

    public ProductCacheService(ProductRepository productRepository,
                                RedisTemplate<String, Object> redisTemplate,
                                ObjectMapper objectMapper) {
        this.productRepository = productRepository;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public List<Product> getAllProducts() {
        Object cachedData = redisTemplate.opsForValue().get(CACHE_KEY);
        if (cachedData != null) {
            log.info("[Cache] Cache HIT cho key: {}", CACHE_KEY);
            try {
                return objectMapper.convertValue(cachedData, new TypeReference<List<Product>>() {});
            } catch (Exception e) {
                log.error("[Cache] Lỗi convert cache data: {}", e.getMessage());
                redisTemplate.delete(CACHE_KEY);
            }
        }
        return refreshCache();
    }

    public synchronized List<Product> refreshCache() {
        log.info("[Cache] Refreshing product cache từ DB...");
        List<Product> products = productRepository.findAllWithDetails();
        redisTemplate.opsForValue().set(CACHE_KEY, products, CACHE_TTL, TimeUnit.MINUTES);
        log.info("[Cache] Cache refreshed — {} sản phẩm", products.size());
        return products;
    }

    /**
     * Được gọi bởi ProductEventListener (Observer) thay vì CommandService trực tiếp.
     * Đây là sự thay đổi key: Command → publish Event → Listener → evict cache.
     */
    public void evictProductsCache() {
        log.info("[Cache] Evicting cache key: {}", CACHE_KEY);
        redisTemplate.delete(CACHE_KEY);
    }
}
