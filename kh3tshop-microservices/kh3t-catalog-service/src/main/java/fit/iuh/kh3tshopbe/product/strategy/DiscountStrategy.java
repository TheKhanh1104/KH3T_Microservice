package fit.iuh.kh3tshopbe.product.strategy;

/**
 * [STRATEGY PATTERN] — Interface định nghĩa hành vi tính giá sau giảm.
 *
 * Lý do dùng Strategy: Logic tính giảm giá trong ProductCommandService cũ
 * bị hardcode thành 1 cách duy nhất (% discount). Khi cần thêm Flash Sale,
 * Fixed Amount Discount, hay Loyalty Discount → phải sửa code gốc (vi phạm OCP).
 *
 * Với Strategy, chỉ cần thêm class implement mới, không đụng code cũ.
 */
public interface DiscountStrategy {

    /**
     * Tính giá thực tế sau khi áp dụng chiến lược giảm giá.
     *
     * @param originalPrice  Giá gốc
     * @param discountValue  Giá trị giảm (% hoặc số tiền cố định tùy implementation)
     * @return Giá sau giảm (cost price)
     */
    double calculateCostPrice(double originalPrice, double discountValue);

    /**
     * Tên chiến lược — dùng để log và debug.
     */
    String getStrategyName();
}
