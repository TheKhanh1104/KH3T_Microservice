package fit.iuh.kh3tshopbe.product.pipeline.filter;

import fit.iuh.kh3tshopbe.product.pipeline.ProductPipelineContext;
import fit.iuh.kh3tshopbe.shared.dto.request.ProductRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * [PIPE & FILTER] — Bước 1: Validate dữ liệu đầu vào.
 *
 * Trách nhiệm: Kiểm tra tính hợp lệ của ProductRequest trước khi xử lý.
 * Nếu không hợp lệ → throw IllegalArgumentException, pipeline dừng lại.
 *
 * Trước đây (ProductCommandService cũ): không có validation tập trung,
 * logic validate rải rác khắp nơi hoặc không có.
 */
@Component
@Order(1)
@Slf4j
public class ValidationFilter implements ProductFilter {

    @Override
    public ProductPipelineContext process(ProductPipelineContext context) {
        log.info("[Pipeline] Bước 1 — ValidationFilter đang chạy...");
        ProductRequest req = context.getRequest();

        if (req.getName() == null || req.getName().isBlank()) {
            throw new IllegalArgumentException("Tên sản phẩm không được để trống");
        }
        if (req.getPrice() <= 0) {
            throw new IllegalArgumentException("Giá sản phẩm phải lớn hơn 0");
        }
        if (req.getDiscountAmount() < 0 || req.getDiscountAmount() > 100) {
            throw new IllegalArgumentException("Giảm giá phải trong khoảng 0–100%");
        }
        if (req.getCategoryRequest() == null || req.getCategoryRequest().getName() == null) {
            throw new IllegalArgumentException("Danh mục sản phẩm không được để trống");
        }
        if (req.getSizeDetailRequests() == null || req.getSizeDetailRequests().isEmpty()) {
            throw new IllegalArgumentException("Sản phẩm phải có ít nhất 1 kích thước");
        }

        log.info("[Pipeline] ValidationFilter PASSED — sản phẩm: {}", req.getName());
        return context; // Pass sang filter tiếp theo
    }

    @Override
    public String getFilterName() {
        return "VALIDATION_FILTER";
    }
}
