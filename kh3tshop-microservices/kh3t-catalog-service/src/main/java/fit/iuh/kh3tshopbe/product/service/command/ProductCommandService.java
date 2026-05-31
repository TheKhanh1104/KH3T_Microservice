package fit.iuh.kh3tshopbe.product.service.command;

import fit.iuh.kh3tshopbe.exception.AppException;
import fit.iuh.kh3tshopbe.exception.ErrorCode;
import fit.iuh.kh3tshopbe.product.event.ProductCreatedEvent;
import fit.iuh.kh3tshopbe.product.event.ProductDeletedEvent;
import fit.iuh.kh3tshopbe.product.event.ProductUpdatedEvent;
import fit.iuh.kh3tshopbe.product.pipeline.ProductPipeline;
import fit.iuh.kh3tshopbe.product.pipeline.ProductPipelineContext;
import fit.iuh.kh3tshopbe.product.repository.ProductRepository;
import fit.iuh.kh3tshopbe.shared.dto.request.ProductRequest;
import fit.iuh.kh3tshopbe.shared.dto.response.CategoryResponse;
import fit.iuh.kh3tshopbe.shared.dto.response.ProductResponse;
import fit.iuh.kh3tshopbe.shared.entity.Product;
import fit.iuh.kh3tshopbe.shared.enums.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * [CQRS — Command Side] Xử lý các thao tác ghi: Create, Update, Delete.
 *
 * ============================================================
 * CẢI TIẾN SO VỚI CODE CŨ:
 * ============================================================
 *
 * 1. [Pipe & Filter] createProduct/updateProduct dùng ProductPipeline:
 *    Trước: 1 method làm mọi thứ (validate + enrich + save + evict)
 *    Sau:   gọi pipeline.execute() → tự động qua ValidationFilter →
 *           EnrichmentFilter → PersistenceFilter
 *
 * 2. [Observer] Không còn gọi evictProductsCache() trực tiếp:
 *    Trước: productCacheService.evictProductsCache()   ← coupling chặt
 *    Sau:   eventPublisher.publishEvent(new ProductCreatedEvent(...))
 *           → ProductEventListener tự lắng nghe và evict cache
 *
 * 3. [Strategy] Logic tính discount nằm trong EnrichmentFilter,
 *    được xử lý bởi DiscountStrategyFactory.
 * ============================================================
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductCommandService {

    private final ProductPipeline productPipeline;
    private final ProductRepository productRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Tạo sản phẩm mới thông qua Pipeline.
     * Pipeline: Validate → Enrich (+ Strategy) → Persist
     * Sau đó publish ProductCreatedEvent → Listener evict cache (Observer).
     */
    public ProductResponse createProduct(ProductRequest request) {
        log.info("[Command] createProduct: {}", request.getName());

        // [Pipe & Filter] Chạy toàn bộ pipeline
        ProductPipelineContext ctx = productPipeline.execute(
                new ProductPipelineContext(request)
        );
        Product saved = ctx.getProduct();

        // [Observer] Publish event thay vì gọi cache trực tiếp
        eventPublisher.publishEvent(new ProductCreatedEvent(this, saved));

        return toResponse(saved);
    }

    /**
     * Cập nhật sản phẩm thông qua Pipeline.
     * Truyền product hiện có vào context để EnrichmentFilter biết là update.
     */
    public ProductResponse updateProduct(int id, ProductRequest request) {
        log.info("[Command] updateProduct id: {}", id);

        Product existing = productRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));

        // [Pipe & Filter] Context update — EnrichmentFilter sẽ biết không tạo mới
        ProductPipelineContext ctx = new ProductPipelineContext(request, id);
        ctx.setProduct(existing);
        ctx = productPipeline.execute(ctx);
        Product updated = ctx.getProduct();

        // [Observer] Publish event
        eventPublisher.publishEvent(new ProductUpdatedEvent(this, updated));

        return toResponse(updated);
    }

    /**
     * Xóa mềm sản phẩm (set INACTIVE).
     */
    public void deleteProduct(int id) {
        log.info("[Command] deleteProduct id: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        product.setStatus(Status.INACTIVE);
        productRepository.save(product);

        // [Observer] Publish event
        eventPublisher.publishEvent(new ProductDeletedEvent(this, id));
    }

    // ─── Mapper nội bộ ──────────────────────────────────────────────────────

    private ProductResponse toResponse(Product p) {
        java.util.List<ProductResponse.SizeDetailResponse> sizeDetails = p.getSizeDetails() == null ? new java.util.ArrayList<>() :
                p.getSizeDetails().stream()
                        .map(sd -> ProductResponse.SizeDetailResponse.builder()
                                .id(sd.getId())
                                .sizeName(sd.getSize().getNameSize().name())
                                .quantity(sd.getQuantity())
                                .build())
                        .collect(Collectors.toList());

        return ProductResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .price(p.getPrice())
                .costPrice(p.getCostPrice())
                .unit(p.getUnit())
                .quantity(p.getQuantity())
                .imageUrlFront(p.getImageUrlFront())
                .imageUrlBack(p.getImageUrlBack())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .rating(p.getRating())
                .discountAmount(p.getDiscountAmount())
                .material(p.getMaterial())
                .form(p.getForm())
                .soldQuantity(0L)
                .status(p.getStatus())
                .category(p.getCategory() == null ? null :
                        CategoryResponse.builder()
                                .id(p.getCategory().getId())
                                .name(p.getCategory().getName())
                                .imageUrl(p.getCategory().getImageUrl())
                                .build())
                .sizeDetails(sizeDetails)
                .build();
    }
}
