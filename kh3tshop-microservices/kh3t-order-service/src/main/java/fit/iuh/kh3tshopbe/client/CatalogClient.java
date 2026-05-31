package fit.iuh.kh3tshopbe.client;

import fit.iuh.kh3tshopbe.dto.response.ApiResponse;
import fit.iuh.kh3tshopbe.dto.response.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "catalog-service")
public interface CatalogClient {

    @GetMapping("/products/{id}")
    ApiResponse<ProductResponse> getProductById(@PathVariable("id") int id);

    @GetMapping("/products/batch")
    ApiResponse<List<ProductResponse>> getProductsByIds(@RequestParam("ids") List<Integer> ids);
}
