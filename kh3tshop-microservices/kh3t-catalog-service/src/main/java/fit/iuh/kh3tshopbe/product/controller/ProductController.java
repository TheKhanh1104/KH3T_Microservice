package fit.iuh.kh3tshopbe.product.controller;

import fit.iuh.kh3tshopbe.product.service.command.ProductCommandService;
import fit.iuh.kh3tshopbe.product.service.query.ProductQueryService;
import fit.iuh.kh3tshopbe.shared.dto.request.ProductRequest;
import fit.iuh.kh3tshopbe.shared.dto.response.ApiResponse;
import fit.iuh.kh3tshopbe.shared.dto.response.ProductResponse;
import fit.iuh.kh3tshopbe.shared.dto.response.TopProductResponse;
import fit.iuh.kh3tshopbe.shared.entity.Product;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * [MODULE: product / LAYERED — Web Layer]
 *
 * Controller chỉ làm 1 việc: nhận HTTP request → gọi đúng service → trả response.
 * Không có business logic ở đây.
 *
 * API endpoints giữ nguyên — không thay đổi so với code cũ:
 *   GET    /products
 *   GET    /products/{id}
 *   GET    /products/batch?ids=...
 *   POST   /products
 *   PUT    /products/{id}
 *   DELETE /products/{id}
 *   GET    /products/top-trending
 *   GET    /products/stats
 */
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductController {

    ProductQueryService productQueryService;
    ProductCommandService productCommandService;

    @GetMapping
    public ApiResponse<List<Product>> getAllProducts() {
        return ApiResponse.<List<Product>>builder()
                .result(productQueryService.getAllProducts())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> getProductById(@PathVariable int id) {
        return ApiResponse.<ProductResponse>builder()
                .result(productQueryService.getProductById(id))
                .build();
    }

    @GetMapping("/batch")
    public ApiResponse<List<ProductResponse>> getProductsByIds(
            @RequestParam("ids") List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return ApiResponse.<List<ProductResponse>>builder()
                    .result(Collections.emptyList())
                    .build();
        }
        return ApiResponse.<List<ProductResponse>>builder()
                .result(productQueryService.getProductsByIds(ids))
                .build();
    }

    @PostMapping
    public ApiResponse<ProductResponse> createProduct(@RequestBody ProductRequest request) {
        return ApiResponse.<ProductResponse>builder()
                .result(productCommandService.createProduct(request))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductResponse> updateProduct(
            @PathVariable int id, @RequestBody ProductRequest request) {
        return ApiResponse.<ProductResponse>builder()
                .result(productCommandService.updateProduct(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Map<String, String>> deleteProduct(@PathVariable int id) {
        productCommandService.deleteProduct(id);
        return ApiResponse.<Map<String, String>>builder()
                .message("Xóa sản phẩm thành công")
                .result(Map.of("status", "success"))
                .build();
    }

    @GetMapping("/top-trending")
    public ResponseEntity<List<TopProductResponse>> getTopTrending(
            @RequestParam(defaultValue = "week") String type) {
        return ResponseEntity.ok(productQueryService.getTopTrending(type));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getStats() {
        return ResponseEntity.ok(productQueryService.getDashboardStats());
    }
}
