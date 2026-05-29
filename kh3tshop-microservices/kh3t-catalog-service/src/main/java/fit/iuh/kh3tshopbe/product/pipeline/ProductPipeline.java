package fit.iuh.kh3tshopbe.product.pipeline;

import fit.iuh.kh3tshopbe.product.pipeline.filter.ProductFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * [PIPE & FILTER] — Orchestrator chạy toàn bộ pipeline theo thứ tự.
 *
 * Spring tự inject List<ProductFilter> đã được sắp xếp theo @Order:
 *   1. ValidationFilter  (@Order(1))
 *   2. EnrichmentFilter  (@Order(2))
 *   3. PersistenceFilter (@Order(3))
 *
 * ProductCommandService chỉ cần gọi:
 *   ProductPipelineContext ctx = pipeline.execute(new ProductPipelineContext(request));
 *   Product saved = ctx.getProduct();
 *
 * Lợi ích:
 * - Thêm filter mới chỉ cần tạo class @Component + @Order → tự động được chèn
 * - Mỗi filter độc lập, dễ test riêng lẻ
 * - Thứ tự rõ ràng, dễ debug
 */
@Component
@Slf4j
public class ProductPipeline {

    private final List<ProductFilter> filters;

    /**
     * Spring inject list filters đã sort theo @Order.
     */
    public ProductPipeline(List<ProductFilter> filters) {
        this.filters = filters;
        log.info("[Pipeline] Khởi tạo ProductPipeline với {} filters: {}",
                filters.size(),
                filters.stream().map(ProductFilter::getFilterName).toList());
    }

    /**
     * Chạy toàn bộ pipeline, mỗi filter xử lý context tuần tự.
     *
     * @param context Context chứa ProductRequest ban đầu
     * @return Context sau khi qua hết tất cả filters (chứa Product đã được save)
     */
    public ProductPipelineContext execute(ProductPipelineContext context) {
        log.info("[Pipeline] ====== Bắt đầu Pipeline ======");
        for (ProductFilter filter : filters) {
            log.info("[Pipeline] Đang chạy: {}", filter.getFilterName());
            context = filter.process(context);
        }
        log.info("[Pipeline] ====== Pipeline hoàn thành ======");
        return context;
    }
}
