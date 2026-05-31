package fit.iuh.kh3tshopbe.client;

import fit.iuh.kh3tshopbe.dto.response.AccountResponse;
import fit.iuh.kh3tshopbe.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "identity-service")
public interface IdentityClient {

    @GetMapping("/accounts/{id}")
    ApiResponse<AccountResponse> getAccountById(@PathVariable("id") int id);

    @GetMapping("/accounts/username/{username}")
    ApiResponse<AccountResponse> getAccountByUsername(@PathVariable("username") String username);
}
