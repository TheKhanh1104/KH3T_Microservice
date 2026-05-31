package fit.iuh.kh3tshopbe.product.pipeline.filter;

import fit.iuh.kh3tshopbe.exception.AppException;
import fit.iuh.kh3tshopbe.exception.ErrorCode;
import fit.iuh.kh3tshopbe.product.pipeline.ProductPipelineContext;
import fit.iuh.kh3tshopbe.product.strategy.DiscountStrategy;
import fit.iuh.kh3tshopbe.product.strategy.DiscountStrategyFactory;
import fit.iuh.kh3tshopbe.shared.dto.request.SizeDetailRequest;
import fit.iuh.kh3tshopbe.shared.entity.*;
import fit.iuh.kh3tshopbe.shared.enums.Status;
import fit.iuh.kh3tshopbe.category.repository.CategoryRepository;
import fit.iuh.kh3tshopbe.size.repository.SizeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * [PIPE & FILTER + STRATEGY] — Bước 2: Enrich (làm giàu dữ liệu).
 *
 * Trách nhiệm: Từ ProductRequest đơn giản, xây dựng Product entity đầy đủ:
 * - Gắn Category từ database
 * - Gắn SizeDetails
 * - Tính costPrice bằng DiscountStrategy (Strategy Pattern!)
 * - Set các trường mặc định: brand, status, rating, dates
 *
 * Tách riêng khỏi Validation và Persistence → mỗi bước chỉ làm 1 việc.
 */
@Component
@Order(2)
@Slf4j
@RequiredArgsConstructor
public class EnrichmentFilter implements ProductFilter {

    private final CategoryRepository categoryRepository;
    private final SizeRepository sizeRepository;
    private final DiscountStrategyFactory discountStrategyFactory;

    @Override
    public ProductPipelineContext process(ProductPipelineContext context) {
        log.info("[Pipeline] Bước 2 — EnrichmentFilter đang chạy...");

        var req = context.getRequest();
        Product product;

        if (context.isUpdate()) {
            // Update: lấy product đã có
            product = context.getProduct();
            product.setName(req.getName());
            product.setDescription(req.getDescription());
            product.setPrice(req.getPrice());
            product.setUnit(req.getUnit());
            product.setImageUrlFront(req.getImageUrlFront());
            product.setImageUrlBack(req.getImageUrlBack());
            product.setDiscountAmount(req.getDiscountAmount());
            product.setMaterial(req.getMaterial());
            product.setForm(req.getForm());
            product.setUpdatedAt(now());
        } else {
            // Create: build mới
            product = Product.builder()
                    .name(req.getName())
                    .description(req.getDescription())
                    .price(req.getPrice())
                    .unit(req.getUnit())
                    .imageUrlFront(req.getImageUrlFront())
                    .imageUrlBack(req.getImageUrlBack())
                    .discountAmount(req.getDiscountAmount())
                    .material(req.getMaterial())
                    .form(req.getForm())
                    .rating(0.0)
                    .brand("HK3T")
                    .status(Status.ACTIVE)
                    .createdAt(now())
                    .updatedAt(now())
                    .build();
        }

        // Gắn Category
        Category category = categoryRepository
                .findByName(req.getCategoryRequest().getName())
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        product.setCategory(category);

        // Tính costPrice bằng Strategy Pattern (thay vì hardcode)
        DiscountStrategy strategy = discountStrategyFactory.getStrategy(req.getDiscountAmount());
        double costPrice = strategy.calculateCostPrice(req.getPrice(), req.getDiscountAmount());
        product.setCostPrice(costPrice);
        log.info("[Pipeline] Dùng chiến lược: {} — costPrice = {}",
                strategy.getStrategyName(), costPrice);

        // Gắn SizeDetails
        List<SizeDetail> sizeDetails = buildSizeDetails(req, product);
        int totalQuantity = sizeDetails.stream().mapToInt(SizeDetail::getQuantity).sum();
        product.setSizeDetails(sizeDetails);
        product.setQuantity(totalQuantity);
        product.setStatus(Status.ACTIVE);

        context.setProduct(product);
        log.info("[Pipeline] EnrichmentFilter DONE — product enriched");
        return context;
    }

    private List<SizeDetail> buildSizeDetails(
            fit.iuh.kh3tshopbe.shared.dto.request.ProductRequest req, Product product) {
        List<SizeDetail> list = new ArrayList<>();
        for (SizeDetailRequest sdr : req.getSizeDetailRequests()) {
            Size size = sizeRepository
                    .findByNameSize(sdr.getSizeRequest().getNameSize())
                    .orElseThrow(() -> new AppException(ErrorCode.UnknownError));
            SizeDetail sd = new SizeDetail();
            sd.setSize(size);
            sd.setProduct(product);
            sd.setQuantity(sdr.getQuantity());
            list.add(sd);
        }
        return list;
    }

    private Date now() {
        return Date.from(LocalDate.now().atStartOfDay()
                .atZone(java.time.ZoneId.systemDefault()).toInstant());
    }

    @Override
    public String getFilterName() {
        return "ENRICHMENT_FILTER";
    }
}
