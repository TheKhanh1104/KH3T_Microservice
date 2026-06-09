package fit.iuh.kh3tshopbe.category.controller;

import fit.iuh.kh3tshopbe.category.service.CategoryService;
import fit.iuh.kh3tshopbe.shared.dto.response.ApiResponse;
import fit.iuh.kh3tshopbe.shared.dto.response.CategoryResponse;
import fit.iuh.kh3tshopbe.shared.dto.response.CategoryRevenueResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * [MODULE: category / LAYERED — Web Layer]
 */
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryController {

    CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {
        ApiResponse<List<CategoryResponse>> body = ApiResponse.<List<CategoryResponse>>builder()
                .result(categoryService.getAllCategories())
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL,
                        CacheControl.maxAge(300, java.util.concurrent.TimeUnit.SECONDS)
                                    .cachePublic().getHeaderValue())
                .body(body);
    }

    @GetMapping("/category-revenue")
    public ApiResponse<List<CategoryRevenueResponse>> getCategoryRevenue() {
        return ApiResponse.<List<CategoryRevenueResponse>>builder()
                .result(categoryService.getCategoryReport())
                .build();
    }
}
