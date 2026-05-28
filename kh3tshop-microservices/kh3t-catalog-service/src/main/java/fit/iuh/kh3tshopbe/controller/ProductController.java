// Updated ProductController.java
package fit.iuh.kh3tshopbe.controller;

import fit.iuh.kh3tshopbe.dto.request.ProductRequest;

import fit.iuh.kh3tshopbe.dto.response.ApiResponse;
import fit.iuh.kh3tshopbe.dto.response.ProductResponse;
import fit.iuh.kh3tshopbe.dto.response.TopProductResponse;
import fit.iuh.kh3tshopbe.entities.Product;
import fit.iuh.kh3tshopbe.service.command.ProductCommandService;
import fit.iuh.kh3tshopbe.service.query.ProductQueryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.web.bind.annotation.*;
import java.util.Collections;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductController {
    ProductQueryService productQueryService;
    ProductCommandService productCommandService;

    @GetMapping
    public ApiResponse<List<Product>> getAllProducts() { // Đổi kiểu trả về để khớp với CacheService
        ApiResponse<List<Product>> response = new ApiResponse<>();
        response.setResult(productQueryService.getAllProducts());
        return response;
    }


    // THÊM: Endpoint cho chi tiết sản phẩm theo ID
    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> getProductById(@PathVariable int id) {
        ApiResponse<ProductResponse> response = new ApiResponse<>();
        response.setResult(productQueryService.getProductById(id));
        return response;
    }
    @GetMapping("/batch")
    public ApiResponse<List<ProductResponse>> getProductsByIds(@RequestParam("ids") List<Integer> ids) {
        ApiResponse<List<ProductResponse>> response = new ApiResponse<>();

        if (ids == null || ids.isEmpty()) {
            // Trả về danh sách rỗng nếu không có ID nào
            response.setResult(Collections.emptyList());
            return response;
        }

        // Gọi tầng Service để lấy danh sách sản phẩm
        response.setResult(productQueryService.getProductsByIds(ids));
        return response;
    }
    @PostMapping
    public ApiResponse<ProductResponse> createProduct(@RequestBody ProductRequest productRequest) {
        ApiResponse<ProductResponse> response = new ApiResponse<>();
        response.setResult(productCommandService.createProduct(productRequest));
        return response;
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductResponse> updateProduct(@PathVariable int id, @RequestBody ProductRequest productRequest) {
        ApiResponse<ProductResponse> response = new ApiResponse<>();
        response.setResult(productCommandService.updateProduct(id, productRequest));
        return response;
    }
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteProduct(@PathVariable int id) {
        productCommandService.deleteProduct(id);
        ApiResponse<String> response = new ApiResponse<>();
        response.setResult("Product deleted successfully");
        return response;
    }


    @GetMapping("/top-trending")
    public List<TopProductResponse> getTopTrending(
            @RequestParam(defaultValue = "week") String type) {
        return productQueryService.getTopTrending(type);
    }

    @GetMapping("/stats")
    public ApiResponse<?> getStats() {
        return ApiResponse.<Object>builder()
            .result(productQueryService.getDashboardStats())
                .build();
    }


}

