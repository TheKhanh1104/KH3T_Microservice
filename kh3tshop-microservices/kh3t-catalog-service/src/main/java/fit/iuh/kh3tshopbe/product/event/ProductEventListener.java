package fit.iuh.kh3tshopbe.product.event;

import fit.iuh.kh3tshopbe.product.cache.ProductCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductEventListener {

    private final ProductCacheService productCacheService;

    /**
     * Khi sản phẩm được tạo → evict cache để lần query tiếp theo lấy data mới.
     */
    @EventListener
    public void handleProductCreated(ProductCreatedEvent event) {
        log.info("[Observer] ProductCreatedEvent received for product: {} — evicting cache",
                event.getProduct().getName());
        productCacheService.evictProductsCache();
    }

    /**
     * Khi sản phẩm được cập nhật → evict cache.
     */
    @EventListener
    public void handleProductUpdated(ProductUpdatedEvent event) {
        log.info("[Observer] ProductUpdatedEvent received for product id: {} — evicting cache",
                event.getProduct().getId());
        productCacheService.evictProductsCache();
    }

    /**
     * Khi sản phẩm bị xóa mềm → evict cache.
     */
    @EventListener
    public void handleProductDeleted(ProductDeletedEvent event) {
        log.info("[Observer] ProductDeletedEvent received for product id: {} — evicting cache",
                event.getProductId());
        productCacheService.evictProductsCache();
    }
}
