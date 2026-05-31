package fit.iuh.kh3tshopbe.product.strategy;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * [STRATEGY PATTERN] — Factory chọn đúng chiến lược giảm giá.
 *
 * ProductCommandService inject factory này, gọi getStrategy() để lấy đúng
 * implementation mà không cần biết chi tiết bên trong.
 *
 * Mở rộng: chỉ cần thêm case mới vào switch — không sửa code cũ.
 */
@Component
@RequiredArgsConstructor
public class DiscountStrategyFactory {

    private final PercentageDiscountStrategy percentageStrategy;
    private final FixedDiscountStrategy fixedStrategy;
    private final NoDiscountStrategy noDiscountStrategy;

    public enum DiscountType {
        PERCENTAGE,   // Giảm theo %
        FIXED,        // Giảm số tiền cố định
        NONE          // Không giảm
    }

    /**
     * Trả về chiến lược phù hợp dựa vào discountAmount.
     * Mặc định dùng PERCENTAGE (tương thích với logic cũ).
     */
    public DiscountStrategy getStrategy(double discountAmount) {
        if (discountAmount <= 0) {
            return noDiscountStrategy;
        }
        // Mặc định: percentage discount (giữ tương thích với code cũ)
        return percentageStrategy;
    }

    /**
     * Lấy chiến lược theo loại tường minh.
     */
    public DiscountStrategy getStrategy(DiscountType type) {
        return switch (type) {
            case PERCENTAGE -> percentageStrategy;
            case FIXED -> fixedStrategy;
            case NONE -> noDiscountStrategy;
        };
    }
}
