package fit.iuh.kh3tshopbe.product.event;

import org.springframework.context.ApplicationEvent;

/**
 * [OBSERVER PATTERN] — Event được publish sau khi xóa mềm sản phẩm.
 */
public class ProductDeletedEvent extends ApplicationEvent {

    private final int productId;

    public ProductDeletedEvent(Object source, int productId) {
        super(source);
        this.productId = productId;
    }

    public int getProductId() {
        return productId;
    }
}
