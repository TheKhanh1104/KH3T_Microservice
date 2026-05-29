package fit.iuh.kh3tshopbe.product.pipeline.filter;

import fit.iuh.kh3tshopbe.product.pipeline.ProductPipelineContext;
import fit.iuh.kh3tshopbe.product.repository.ProductRepository;
import fit.iuh.kh3tshopbe.shared.entity.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * [PIPE & FILTER] — Bước 3: Lưu Product vào database.
 *
 * Trách nhiệm duy nhất: Gọi repository.save() và cập nhật product trong context.
 * Không quan tâm đến validation hay enrichment — đã được làm ở các bước trước.
 */
@Component
@Order(3)
@Slf4j
@RequiredArgsConstructor
public class PersistenceFilter implements ProductFilter {

    private final ProductRepository productRepository;

    @Override
    public ProductPipelineContext process(ProductPipelineContext context) {
        log.info("[Pipeline] Bước 3 — PersistenceFilter đang chạy...");

        Product saved = productRepository.save(context.getProduct());
        context.setProduct(saved);

        log.info("[Pipeline] PersistenceFilter DONE — saved product id: {}", saved.getId());
        return context;
    }

    @Override
    public String getFilterName() {
        return "PERSISTENCE_FILTER";
    }
}
