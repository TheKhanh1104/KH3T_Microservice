package fit.iuh.kh3tshopbe.product.strategy;

import org.springframework.stereotype.Component;

/**
 * [STRATEGY PATTERN] — Chiến lược không giảm giá.
 * Dùng khi sản phẩm không có khuyến mãi (discountAmount = 0).
 */
@Component
public class NoDiscountStrategy implements DiscountStrategy {

    @Override
    public double calculateCostPrice(double originalPrice, double discountValue) {
        return originalPrice;
    }

    @Override
    public String getStrategyName() {
        return "NO_DISCOUNT";
    }
}
