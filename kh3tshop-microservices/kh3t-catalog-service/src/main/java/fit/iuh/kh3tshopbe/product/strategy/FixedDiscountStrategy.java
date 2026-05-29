package fit.iuh.kh3tshopbe.product.strategy;

import org.springframework.stereotype.Component;

/**
 * [STRATEGY PATTERN] — Chiến lược giảm số tiền cố định.
 *
 * Ví dụ: giá 500.000đ, discountValue = 50.000 (đồng)
 * → costPrice = 500.000 - 50.000 = 450.000đ
 *
 * Dùng cho chương trình giảm giá sự kiện (Flash Sale) với số tiền cố định.
 */
@Component
public class FixedDiscountStrategy implements DiscountStrategy {

    @Override
    public double calculateCostPrice(double originalPrice, double discountValue) {
        if (discountValue <= 0) return originalPrice;
        double result = originalPrice - discountValue;
        return Math.max(result, 0); // Không cho âm
    }

    @Override
    public String getStrategyName() {
        return "FIXED_DISCOUNT";
    }
}
