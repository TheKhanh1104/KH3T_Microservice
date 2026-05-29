package fit.iuh.kh3tshopbe.product.controller;

import fit.iuh.kh3tshopbe.product.service.command.ProductCommandService;
import fit.iuh.kh3tshopbe.product.service.query.ProductQueryService;
import fit.iuh.kh3tshopbe.shared.dto.request.ProductRequest;
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
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productQueryService.getAllProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable int id) {
        return ResponseEntity.ok(productQueryService.getProductById(id));
    }

    @GetMapping("/batch")
    public ResponseEntity<List<ProductResponse>> getProductsByIds(
            @RequestParam("ids") List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        return ResponseEntity.ok(productQueryService.getProductsByIds(ids));
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@RequestBody ProductRequest request) {
        return ResponseEntity.ok(productCommandService.createProduct(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable int id, @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productCommandService.updateProduct(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteProduct(@PathVariable int id) {
        productCommandService.deleteProduct(id);
        return ResponseEntity.ok(Map.of("message", "Xóa sản phẩm thành công"));
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
