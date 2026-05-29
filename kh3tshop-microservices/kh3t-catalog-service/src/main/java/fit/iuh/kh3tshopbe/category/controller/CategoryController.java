package fit.iuh.kh3tshopbe.category.controller;

import fit.iuh.kh3tshopbe.category.service.CategoryService;
import fit.iuh.kh3tshopbe.shared.dto.response.CategoryResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

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
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/category-revenue")
    public ResponseEntity<Map<String, Object>> getCategoryRevenue() {
        return ResponseEntity.ok(Map.of("code", 200, "result", categoryService.getCategoryReport()));
    }
}
