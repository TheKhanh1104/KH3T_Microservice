package fit.iuh.kh3tshopbe.product.service.query;

import fit.iuh.kh3tshopbe.product.cache.ProductCacheService;
import fit.iuh.kh3tshopbe.product.repository.ProductRepository;
import fit.iuh.kh3tshopbe.shared.dto.response.CategoryResponse;
import fit.iuh.kh3tshopbe.shared.dto.response.ProductResponse;
import fit.iuh.kh3tshopbe.shared.dto.response.TopProductResponse;
import fit.iuh.kh3tshopbe.shared.entity.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * [CQRS — Query Side] Chỉ đọc dữ liệu — không thay đổi trạng thái.
 *
 * Đây là điểm mấu chốt của CQRS:
 * - Command side (ProductCommandService): ghi vào DB, publish event
 * - Query side (ProductQueryService): đọc từ Cache (Redis) hoặc DB
 *
 * Query side KHÔNG bao giờ modify data, KHÔNG publish event.
 * Điều này cho phép optimize riêng: query có thể đọc từ Redis,
 * trong khi command luôn ghi vào MariaDB (primary).
 *
 * [MODULE: product] — Thuộc module product.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductQueryService {

    private final ProductRepository productRepository;
    private final ProductCacheService productCacheService;

    /**
     * Lấy tất cả sản phẩm — ưu tiên từ Redis Cache (Cache-Aside).
     */
    public List<Product> getAllProducts() {
        log.info("[Query] getAllProducts — đọc trực tiếp từ DB để kiểm tra...");
        return productRepository.findAllWithDetails();
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(int id) {
        log.info("======= [DEBUG CATALOG] Đang tìm sản phẩm ID: {} =======", id);
        
        // 1. Tìm đơn giản nhất trước
        Product product = productRepository.findById(id)
                .orElse(null);

        if (product == null) {
            log.error("❌ [DEBUG CATALOG] Không tìm thấy ID {} trong DB kh3t_catalog!", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm ID: " + id);
        }

        log.info("✅ [DEBUG CATALOG] Tìm thấy sản phẩm: {}. Đang convert...", product.getName());
        
        try {
            return toResponse(product, 0L);
        } catch (Exception e) {
            log.error("❌ [DEBUG CATALOG] Lỗi mapping sản phẩm {}: {}", id, e.getMessage());
            e.printStackTrace();
            // Trả về bản tối giản để không bị Product not found
            return ProductResponse.builder()
                    .id(product.getId())
                    .name(product.getName())
                    .price(product.getPrice())
                    .imageUrlFront(product.getImageUrlFront())
                    .description(product.getDescription())
                    .build();
        }
    }

    /**
     * Lấy nhiều sản phẩm theo danh sách ID (dùng cho Order Service).
     */
    public List<ProductResponse> getProductsByIds(List<Integer> ids) {
        log.info("[Query] getProductsByIds: {} ids", ids.size());
        return productRepository.findAllById(ids).stream()
                .map(p -> toResponse(p, 0L))
                .collect(Collectors.toList());
    }

    /**
     * Lấy sản phẩm đang giảm giá.
     */
    public List<Product> getSaleProducts() {
        return productRepository.findByDiscountAmountGreaterThan(0.1);
    }

    /**
     * Top trending (placeholder — dữ liệu bán hàng nằm ở Order Service).
     */
    public List<TopProductResponse> getTopTrending(String type) {
        return Collections.emptyList();
    }

    /**
     * Dashboard stats.
     */
    public Map<String, Long> getDashboardStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("totalProducts", productRepository.getTotalProducts());
        stats.put("lowStock", productRepository.getLowStockProducts(10));
        return stats;
    }

    // ─── Mapper nội bộ (Đã thêm kiểm tra Null an toàn) ──────────────────────

    private ProductResponse toResponse(Product p, Long soldQuantity) {
        List<ProductResponse.SizeDetailResponse> sizeDetails = new ArrayList<>();
        
        try {
            if (p.getSizeDetails() != null) {
                sizeDetails = p.getSizeDetails().stream()
                        .filter(sd -> sd != null)
                        .map(sd -> {
                            String name = "N/A";
                            if (sd.getSize() != null && sd.getSize().getNameSize() != null) {
                                name = sd.getSize().getNameSize().name();
                            }
                            return ProductResponse.SizeDetailResponse.builder()
                                    .id(sd.getId())
                                    .sizeName(name)
                                    .quantity(sd.getQuantity())
                                    .build();
                        })
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.warn("Lỗi khi load SizeDetails cho sản phẩm {}: {}", p.getId(), e.getMessage());
        }

        CategoryResponse categoryResp = null;
        try {
            if (p.getCategory() != null) {
                categoryResp = CategoryResponse.builder()
                        .id(p.getCategory().getId())
                        .name(p.getCategory().getName())
                        .imageUrl(p.getCategory().getImageUrl())
                        .build();
            }
        } catch (Exception e) {
            log.warn("Lỗi khi load Category cho sản phẩm {}: {}", p.getId(), e.getMessage());
        }

        return ProductResponse.builder()
                .id(p.getId())
                .name(p.getName() != null ? p.getName() : "Không tên")
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
                .soldQuantity(soldQuantity)
                .status(p.getStatus())
                .category(categoryResp)
                .sizeDetails(sizeDetails)
                .build();
    }
}
