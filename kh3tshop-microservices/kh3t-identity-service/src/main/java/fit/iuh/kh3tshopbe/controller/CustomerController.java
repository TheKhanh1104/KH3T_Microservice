package fit.iuh.kh3tshopbe.controller;

import fit.iuh.kh3tshopbe.dto.response.ApiResponse;
import fit.iuh.kh3tshopbe.dto.response.CustomerResponse;
import fit.iuh.kh3tshopbe.dto.request.CustomerUpdateRequest;
import fit.iuh.kh3tshopbe.dto.response.AccountResponse;
import fit.iuh.kh3tshopbe.service.CustomerService;
import fit.iuh.kh3tshopbe.service.AccountService;
import fit.iuh.kh3tshopbe.exception.AppException;
import fit.iuh.kh3tshopbe.exception.ErrorCode;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomerController {
    CustomerService customerService;
    AccountService accountService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ApiResponse<List<CustomerResponse>> getCustomers() {
        ApiResponse<List<CustomerResponse>> customerResponseApiResponse = new ApiResponse<>();
        customerResponseApiResponse.setResult(customerService.getAllCustomers());
        return customerResponseApiResponse;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/profile")
    public ApiResponse<CustomerResponse> getCurrentCustomerProfile() {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();

        // 1. Lấy AccountResponse
        AccountResponse accountResponse = accountService.getAccountByUsername(currentUsername);

        // 2. Kiểm tra NULL TRƯỚC KHI TRUY CẬP .getCustomer()
        if (accountResponse == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        CustomerResponse customerResponse = accountResponse.getCustomer();

        if (customerResponse == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        int customerId = customerResponse.getId();

        ApiResponse<CustomerResponse> response = new ApiResponse<>();
        response.setResult(customerService.getCurrentCustomerProfile(customerId));
        response.setCode(200);
        response.setMessage("Lấy hồ sơ thành công");
        return response;
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/update-profile")
    public ApiResponse<CustomerResponse> updateProfile(@RequestBody @Valid CustomerUpdateRequest request) {
        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        AccountResponse accountResponse = accountService.getAccountByUsername(currentUsername);
        if (accountResponse == null || accountResponse.getCustomer() == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }
        int currentCustomerId = accountResponse.getCustomer().getId();
        if (!request.getId().equals(currentCustomerId)) {
            throw new AppException(ErrorCode.User_Not_Authorized);
        }

        ApiResponse<CustomerResponse> response = new ApiResponse<>();
        response.setResult(customerService.updateCustomerProfile(request));
        response.setCode(200);
        response.setMessage("Updated Profile Successful!");
        return response;
    }

    @GetMapping("/{id}")
    public CustomerResponse getCustomerById(@PathVariable int id) {
        return customerService.getCustomerById(id);
    }
}
