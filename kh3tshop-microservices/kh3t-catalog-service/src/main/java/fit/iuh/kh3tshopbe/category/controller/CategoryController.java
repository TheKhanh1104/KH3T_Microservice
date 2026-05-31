package fit.iuh.kh3tshopbe.category.controller;

import fit.iuh.kh3tshopbe.category.service.CategoryService;
import fit.iuh.kh3tshopbe.shared.dto.response.ApiResponse;
import fit.iuh.kh3tshopbe.shared.dto.response.CategoryResponse;
import fit.iuh.kh3tshopbe.shared.dto.response.CategoryRevenueResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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
    public ApiResponse<List<CategoryResponse>> getAllCategories() {
        return ApiResponse.<List<CategoryResponse>>builder()
                .result(categoryService.getAllCategories())
                .build();
    }

    @GetMapping("/category-revenue")
    public ApiResponse<List<CategoryRevenueResponse>> getCategoryRevenue() {
        return ApiResponse.<List<CategoryRevenueResponse>>builder()
                .result(categoryService.getCategoryReport())
                .build();
    }
}
