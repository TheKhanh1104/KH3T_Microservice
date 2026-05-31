package fit.iuh.kh3tshopbe.product.event;

import fit.iuh.kh3tshopbe.shared.entity.Product;
import org.springframework.context.ApplicationEvent;

/**
 * [OBSERVER PATTERN] — Event được publish sau khi cập nhật sản phẩm thành công.
 */
public class ProductUpdatedEvent extends ApplicationEvent {

    private final Product product;

    public ProductUpdatedEvent(Object source, Product product) {
        super(source);
        this.product = product;
    }

    public Product getProduct() {
        return product;
    }
}
