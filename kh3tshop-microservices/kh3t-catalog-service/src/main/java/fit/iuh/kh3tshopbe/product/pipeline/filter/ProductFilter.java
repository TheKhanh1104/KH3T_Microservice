package fit.iuh.kh3tshopbe.product.pipeline.filter;

import fit.iuh.kh3tshopbe.product.pipeline.ProductPipelineContext;

/**
 * [PIPE & FILTER] — Interface cho mỗi bước xử lý trong pipeline.
 *
 * Mỗi Filter chỉ làm 1 nhiệm vụ cụ thể (Single Responsibility),
 * và nhận dữ liệu từ context, xử lý, rồi ghi kết quả lại vào context.
 *
 * Thứ tự thực thi: ValidationFilter → EnrichmentFilter → PersistenceFilter
 */
public interface ProductFilter {

    /**
     * Xử lý context và trả về context đã được biến đổi.
     * Nếu có lỗi, throw exception để dừng pipeline.
     */
    ProductPipelineContext process(ProductPipelineContext context);

    /**
     * Tên filter — dùng để log thứ tự thực thi.
     */
    String getFilterName();
}
