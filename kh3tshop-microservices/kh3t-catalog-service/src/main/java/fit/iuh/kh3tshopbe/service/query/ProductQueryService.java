package fit.iuh.kh3tshopbe.service.query;

import fit.iuh.kh3tshopbe.dto.response.CategoryResponse;
import fit.iuh.kh3tshopbe.dto.response.ProductResponse;
import fit.iuh.kh3tshopbe.dto.response.ProductResponse.SizeDetailResponse;
import fit.iuh.kh3tshopbe.dto.response.TopProductResponse;
import fit.iuh.kh3tshopbe.entities.Product;
import fit.iuh.kh3tshopbe.repository.ProductRepository;
import fit.iuh.kh3tshopbe.service.ProductCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductQueryService {

    private final ProductRepository productRepository;
    private final ProductCacheService productCacheService;

    public List<Product> getAllProducts() {
        return productCacheService.getAllProducts();
    }

    public ProductResponse getProductById(int id) {
        Product product = productRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found with id: " + id));
        return toResponse(product, 0L);
    }

    public List<ProductResponse> getProductsByIds(List<Integer> ids) {
        return productRepository.findAllById(ids).stream()
                .map(product -> toResponse(product, 0L))
                .collect(Collectors.toList());
    }

    public List<Product> getSaleProducts() {
        return productRepository.findByDiscountAmountGreaterThan(0.1);
    }

    public List<TopProductResponse> getTopTrending(String type) {
        return Collections.emptyList();
    }

    public java.util.Map<String, Long> getDashboardStats() {
        java.util.Map<String, Long> stats = new java.util.HashMap<>();
        stats.put("totalProducts", productRepository.getTotalProducts());
        stats.put("lowStock", productRepository.getLowStockProducts(10));
        return stats;
    }

    private ProductResponse toResponse(Product product, Long soldQuantity) {
        List<SizeDetailResponse> sizeDetailResponses = product.getSizeDetails().stream()
                .map(sd -> SizeDetailResponse.builder()
                        .id(sd.getId())
                        .sizeName(sd.getSize().getNameSize().name())
                        .quantity(sd.getQuantity())
                        .build())
                .collect(Collectors.toList());

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .costPrice(product.getCostPrice())
                .unit(product.getUnit())
                .quantity(product.getQuantity())
                .imageUrlFront(product.getImageUrlFront())
                .imageUrlBack(product.getImageUrlBack())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .rating(product.getRating())
                .discountAmount(product.getDiscountAmount())
                .material(product.getMaterial())
                .form(product.getForm())
                .soldQuantity(soldQuantity)
                .status(product.getStatus())
                .category(CategoryResponse.builder()
                        .id(product.getCategory().getId())
                        .name(product.getCategory().getName())
                        .imageUrl(product.getCategory().getImageUrl())
                        .build())
                .sizeDetails(sizeDetailResponses)
                .build();
    }
}
