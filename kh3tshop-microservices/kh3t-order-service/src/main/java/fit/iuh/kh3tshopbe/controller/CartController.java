package fit.iuh.kh3tshopbe.controller;

import fit.iuh.kh3tshopbe.dto.request.CartUpdateRequest;
import fit.iuh.kh3tshopbe.dto.request.CartRequest;
import fit.iuh.kh3tshopbe.dto.response.ApiResponse;
import fit.iuh.kh3tshopbe.dto.response.CartResponse;
import fit.iuh.kh3tshopbe.entities.Cart;
import fit.iuh.kh3tshopbe.service.CartService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/carts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class CartController {
    CartService cartService;

    @GetMapping("/account/{accountId}")
    public ApiResponse<CartResponse> getCartByAccountId(@PathVariable String accountId) {
        log.info("[Order Service] Request giỏ hàng cho Account: {}", accountId);
        try {
            // Frontend có thể gửi ID số (13) hoặc UUID. Thử parse về int.
            int id = Integer.parseInt(accountId);
            Cart cart = cartService.getCartByAccountId(id);

            CartResponse response = CartResponse.builder()
                    .id(cart.getId())
                    .totalQuantity(cart.getTotalQuantity())
                    .totalAmount(cart.getTotalAmount())
                    .build();

            return ApiResponse.<CartResponse>builder().result(response).build();
        } catch (Exception e) {
            log.warn("[Order Service] Không thể lấy giỏ hàng cho ID {}: {}. Trả về giỏ hàng trống.", accountId, e.getMessage());
            // Trả về giỏ hàng trống thay vì báo lỗi 500 để Frontend không bị crash
            CartResponse emptyCart = CartResponse.builder()
                    .id(0)
                    .totalQuantity(0)
                    .totalAmount(0.0)
                    .build();
            return ApiResponse.<CartResponse>builder().result(emptyCart).build();
        }
    }

    @PutMapping("/update/{cartId}")
    public ApiResponse<CartResponse> updateCart(@PathVariable int cartId, @RequestBody CartRequest cartRequest) {
        CartResponse updated = cartService.updateCart(cartId, cartRequest);
        return ApiResponse.<CartResponse>builder()
                .message("Cart updated")
                .result(updated)
                .build();
    }

    @PutMapping("/update/{cartId}/increase")
    public ApiResponse<CartResponse> updateCartIncrease(@PathVariable int cartId, @RequestBody CartUpdateRequest cartPriceRequest) {
        CartResponse cartResponse = cartService.updateCartIncrease(cartId, cartPriceRequest);
        return ApiResponse.<CartResponse>builder()
                .message("Cart updated")
                .result(cartResponse)
                .build();
    }

    @PutMapping("/update/{cartId}/decrease")
    public ApiResponse<CartResponse> updateCartDecrease(@PathVariable int cartId, @RequestBody CartUpdateRequest cartPriceRequest) {
        CartResponse cartResponse = cartService.updateCartDecrease(cartId, cartPriceRequest);
        return ApiResponse.<CartResponse>builder()
                .message("Cart updated")
                .result(cartResponse)
                .build();
    }

    @PutMapping("/update/{cartId}/delete")
    public ApiResponse<CartResponse> updateCartDelete(@PathVariable int cartId, @RequestBody CartUpdateRequest cartPriceRequest) {
        CartResponse cartResponse = cartService.updateCartDelete(cartId, cartPriceRequest);
        return ApiResponse.<CartResponse>builder()
                .message("Cart updated")
                .result(cartResponse)
                .build();
    }
}
