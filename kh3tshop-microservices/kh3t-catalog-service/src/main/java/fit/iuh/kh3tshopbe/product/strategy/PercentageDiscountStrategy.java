package fit.iuh.kh3tshopbe.product.strategy;

import org.springframework.stereotype.Component;

/**
 * [STRATEGY PATTERN] — Chiến lược giảm theo phần trăm (%).
 *
 * Ví dụ: giá 500.000đ, discountAmount = 20 (%)
 * → costPrice = 500.000 - (500.000 * 20 / 100) = 400.000đ
 *
 * Đây là chiến lược mặc định, thay thế logic hardcode cũ trong ProductCommandService.
 */
@Component
public class PercentageDiscountStrategy implements DiscountStrategy {

    @Override
    public double calculateCostPrice(double originalPrice, double discountValue) {
        if (discountValue <= 0) return originalPrice;
        if (discountValue >= 100) return 0;
        return originalPrice - (originalPrice * discountValue / 100);
    }

    @Override
    public String getStrategyName() {
        return "PERCENTAGE_DISCOUNT";
    }
}
