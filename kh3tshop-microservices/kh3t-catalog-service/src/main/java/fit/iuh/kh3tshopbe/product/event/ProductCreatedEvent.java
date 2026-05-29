package fit.iuh.kh3tshopbe.product.event;

import fit.iuh.kh3tshopbe.shared.entity.Product;
import org.springframework.context.ApplicationEvent;

/**
 * [OBSERVER PATTERN — Spring ApplicationEvent]
 *
 * Event được publish sau khi tạo sản phẩm thành công.
 *
 * Lý do dùng Observer ở đây:
 * - Trước đây: ProductCommandService phải tự gọi evictProductsCache() → coupling chặt
 * - Sau khi dùng Observer: Command chỉ publish event, không cần biết ai lắng nghe
 * - ProductEventListener tự nhận event và evict cache → Separation of Concerns
 *
 * Lợi ích: Sau này muốn gửi email thông báo, log audit, sync Elasticsearch...
 * chỉ cần thêm @EventListener mới — không sửa CommandService.
 */
public class ProductCreatedEvent extends ApplicationEvent {

    private final Product product;

    public ProductCreatedEvent(Object source, Product product) {
        super(source);
        this.product = product;
    }

    public Product getProduct() {
        return product;
    }
}
