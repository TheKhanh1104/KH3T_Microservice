package fit.iuh.kh3tshopbe.service;

import fit.iuh.kh3tshopbe.client.CatalogClient;
import fit.iuh.kh3tshopbe.dto.request.CartDetailRequest;
import fit.iuh.kh3tshopbe.dto.response.CartDetailResponse;
import fit.iuh.kh3tshopbe.dto.response.ProductResponse;
import fit.iuh.kh3tshopbe.dto.response.ApiResponse;
import fit.iuh.kh3tshopbe.entities.Cart;
import fit.iuh.kh3tshopbe.entities.CartDetail;
import fit.iuh.kh3tshopbe.mapper.CartDetailMapper;
import fit.iuh.kh3tshopbe.repository.CartDetailRepository;
import fit.iuh.kh3tshopbe.repository.CartRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class CartDetailService {
    CartDetailRepository cartDetailRepository;
    CartDetailMapper cartDetailMapper;
    CartRepository cartRepository;
    CatalogClient catalogClient;

    public CartDetailResponse addCartDetail(CartDetailRequest cartDetailRequest) {
        ApiResponse<ProductResponse> productRes = catalogClient.getProductById(cartDetailRequest.getProductId());
        if (productRes == null || productRes.getResult() == null) {
            throw new RuntimeException("Product not found");
        }
        ProductResponse product = productRes.getResult();

        Cart cart = cartRepository.findById(cartDetailRequest.getCartId())
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        ProductResponse.SizeDetailResponse sizeDetail = product.getSizeDetails().stream()
                .filter(sd -> sd.getId() == cartDetailRequest.getSizeDetailId())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Size not found"));

        CartDetail existing = cartDetailRepository.findByCartAndProductIdAndSizeDetailId(cart, product.getId(), sizeDetail.getId());

        CartDetail saved;
        if (existing != null) {
            int newQuantity = existing.getQuantity() + cartDetailRequest.getQuantity();
            existing.setQuantity(newQuantity);
            existing.setSubtotal(existing.getPrice_at_time() * newQuantity);
            existing.setUpdateAt(new Date());
            saved = cartDetailRepository.save(existing);
        } else {
            CartDetail cartDetail = new CartDetail();
            cartDetail.setProductId(product.getId());
            cartDetail.setCart(cart);
            cartDetail.setSizeDetailId(sizeDetail.getId());
            cartDetail.setQuantity(cartDetailRequest.getQuantity() > 0 ? cartDetailRequest.getQuantity() : 1);
            cartDetail.setSelected(false);
            cartDetail.setUpdateAt(null);
            cartDetail.setCreateAt(new Date());
            cartDetail.setSubtotal(product.getPrice() * cartDetail.getQuantity());
            cartDetail.setPrice_at_time(product.getPrice());
            saved = cartDetailRepository.save(cartDetail);
        }

        CartDetailResponse response = cartDetailMapper.toCartDetailResponse(saved);
        enrichDetail(response, product);
        return response;
    }

    public List<CartDetailResponse> getCartDetailListByCardId(int cartId){
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        List<CartDetail> cartDetailList = cartDetailRepository.findByCart(cart);

        List<CartDetailResponse> responseList = cartDetailList.stream()
                .map(cartDetailMapper::toCartDetailResponse)
                .collect(Collectors.toList());

        enrichDetails(responseList);
        return responseList;
    }

    public CartDetailResponse updateCartDetailSelected(int cartDetailId, boolean selected) {
        CartDetail cartDetail = cartDetailRepository.findById(cartDetailId)
                .orElseThrow(() -> new RuntimeException("CartDetail not found"));
        cartDetail.setSelected(selected);
        cartDetail.setUpdateAt(new Date());
        CartDetail updated = cartDetailRepository.save(cartDetail);
        
        CartDetailResponse response = cartDetailMapper.toCartDetailResponse(updated);
        ApiResponse<ProductResponse> productRes = catalogClient.getProductById(updated.getProductId());
        if (productRes != null && productRes.getResult() != null) {
            enrichDetail(response, productRes.getResult());
        }
        return response;
    }

    public CartDetailResponse updateCartDetailIncreaseQuantity(int cartDetailId){
        CartDetail cartDetail = cartDetailRepository
                .findById(cartDetailId)
                .orElseThrow(() -> new RuntimeException("CartDetail not found"));
        cartDetail.setQuantity(cartDetail.getQuantity() + 1);
        cartDetail.setSubtotal(cartDetail.getPrice_at_time() * cartDetail.getQuantity());
        cartDetail.setUpdateAt(new Date());
        CartDetail updated = cartDetailRepository.save(cartDetail);

        CartDetailResponse response = cartDetailMapper.toCartDetailResponse(updated);
        ApiResponse<ProductResponse> productRes = catalogClient.getProductById(updated.getProductId());
        if (productRes != null && productRes.getResult() != null) {
            enrichDetail(response, productRes.getResult());
        }
        return response;
    }

    public CartDetailResponse updateCartDetailDecreaseQuantity(int cartDetailId){
        CartDetail cartDetail = cartDetailRepository
                .findById(cartDetailId)
                .orElseThrow(() -> new RuntimeException("CartDetail not found"));
        cartDetail.setQuantity(cartDetail.getQuantity() - 1);
        cartDetail.setSubtotal(cartDetail.getPrice_at_time() * cartDetail.getQuantity());
        cartDetail.setUpdateAt(new Date());
        CartDetail updated = cartDetailRepository.save(cartDetail);

        CartDetailResponse response = cartDetailMapper.toCartDetailResponse(updated);
        ApiResponse<ProductResponse> productRes = catalogClient.getProductById(updated.getProductId());
        if (productRes != null && productRes.getResult() != null) {
            enrichDetail(response, productRes.getResult());
        }
        return response;
    }

    public void deleteCartDetail(int cartDetailId) {
        CartDetail cartDetail = cartDetailRepository.findById(cartDetailId)
                .orElseThrow(() -> new RuntimeException("CartDetail not found"));
        cartDetailRepository.delete(cartDetail);
    }

    public List<CartDetailResponse> getCartDetailIsSelected(int cartId){
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new RuntimeException("Cart not found"));
        List<CartDetail> selectedList = cartDetailRepository.findByIsSelectedAndCart(true, cart);
        
        List<CartDetailResponse> responseList = selectedList.stream()
                .map(cartDetailMapper::toCartDetailResponse)
                .collect(Collectors.toList());

        enrichDetails(responseList);
        return responseList;
    }

    private void enrichDetails(List<CartDetailResponse> list) {
        if (list.isEmpty()) return;
        List<Integer> ids = list.stream().map(CartDetailResponse::getProductId).distinct().collect(Collectors.toList());
        ApiResponse<List<ProductResponse>> response = catalogClient.getProductsByIds(ids);
        if (response != null && response.getResult() != null) {
            Map<Integer, ProductResponse> productMap = response.getResult().stream()
                    .collect(Collectors.toMap(ProductResponse::getId, p -> p));
            for (CartDetailResponse d : list) {
                ProductResponse p = productMap.get(d.getProductId());
                if (p != null) {
                    enrichDetail(d, p);
                }
            }
        }
    }

    private void enrichDetail(CartDetailResponse d, ProductResponse p) {
        d.setProductName(p.getName());
        d.setProductImage(p.getImageUrlFront());
        p.getSizeDetails().stream()
                .filter(sd -> sd.getId() == d.getSizeDetailId())
                .findFirst()
                .ifPresent(sd -> d.setSizeName(sd.getSizeName()));
    }
}
