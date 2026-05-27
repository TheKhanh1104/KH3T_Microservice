package fit.iuh.kh3tshopbe.service;

import fit.iuh.kh3tshopbe.client.CatalogClient;
import fit.iuh.kh3tshopbe.client.IdentityClient;
import fit.iuh.kh3tshopbe.dto.response.AccountResponse;
import fit.iuh.kh3tshopbe.dto.response.ApiResponse;
import fit.iuh.kh3tshopbe.dto.response.ProductResponse;
import fit.iuh.kh3tshopbe.dto.response.WishListDetailResponse;
import fit.iuh.kh3tshopbe.entities.WishList;
import fit.iuh.kh3tshopbe.entities.WishListDetail;
import fit.iuh.kh3tshopbe.repository.WishListDetailRepository;
import fit.iuh.kh3tshopbe.repository.WishListRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WishListDetailService {

    WishListDetailRepository detailRepository;
    WishListRepository wishlistRepository;
    CatalogClient catalogClient;
    IdentityClient identityClient;

    private int getAccountIdFromUsername(String username) {
        ApiResponse<AccountResponse> accountRes = identityClient.getAccountByUsername(username);
        if (accountRes == null || accountRes.getResult() == null) {
            throw new RuntimeException("User not found");
        }
        return accountRes.getResult().getId();
    }

    @Transactional
    public void removeItem(Integer wishlistId, Integer productId, String username) {
        int accountId = getAccountIdFromUsername(username);
        WishList wishlist = wishlistRepository.findById(wishlistId)
                .orElseThrow(() -> new RuntimeException("Wishlist not found"));

        if (wishlist.getAccountId() != accountId) {
            throw new RuntimeException("Không có quyền xóa");
        }

        if (!detailRepository.existsByWishlist_IdAndProductId(wishlistId, productId)) {
            throw new RuntimeException("Sản phẩm không có trong wishlist");
        }

        detailRepository.deleteByWishlist_IdAndProductId(wishlistId, productId);
    }

    public List<WishListDetailResponse> getItemsByWishlistId(Integer wishlistId, String username) {
        int accountId = getAccountIdFromUsername(username);
        WishList wishlist = wishlistRepository.findById(wishlistId)
                .orElseThrow(() -> new RuntimeException("Wishlist not found"));

        if (wishlist.getAccountId() != accountId) {
            throw new RuntimeException("Không có quyền truy cập wishlist này");
        }

        List<WishListDetail> details = detailRepository.findByWishlist_Id(wishlistId);
        if (details.isEmpty()) {
            return Collections.emptyList();
        }

        List<Integer> productIds = details.stream().map(WishListDetail::getProductId).distinct().collect(Collectors.toList());
        ApiResponse<List<ProductResponse>> productRes = catalogClient.getProductsByIds(productIds);
        Map<Integer, ProductResponse> productMap = new HashMap<>();
        if (productRes != null && productRes.getResult() != null) {
            productMap = productRes.getResult().stream().collect(Collectors.toMap(ProductResponse::getId, p -> p));
        }

        List<WishListDetailResponse> responses = new ArrayList<>();
        for (WishListDetail d : details) {
            ProductResponse p = productMap.get(d.getProductId());
            WishListDetailResponse.WishListDetailResponseBuilder builder = WishListDetailResponse.builder()
                    .id(d.getId())
                    .note(d.getNote())
                    .created_at(d.getCreated_at())
                    .wishlistId(d.getWishlist().getId())
                    .productId(d.getProductId());

            if (p != null) {
                builder.productName(p.getName())
                        .productImage(p.getImageUrlFront())
                        .productPrice(p.getPrice())
                        .discountAmount((int) p.getDiscountAmount());
            }
            responses.add(builder.build());
        }
        return responses;
    }

    @Transactional
    public void addItem(Integer wishlistId, Integer productId, String username) {
        int accountId = getAccountIdFromUsername(username);
        WishList wishlist = wishlistRepository.findById(wishlistId)
                .orElseThrow(() -> new RuntimeException("Wishlist không tồn tại"));

        if (wishlist.getAccountId() != accountId) {
            throw new RuntimeException("Bạn không có quyền thêm vào wishlist này");
        }

        ApiResponse<ProductResponse> productRes = catalogClient.getProductById(productId);
        if (productRes == null || productRes.getResult() == null) {
            throw new RuntimeException("Sản phẩm không tồn tại");
        }

        boolean exists = detailRepository.existsByWishlist_IdAndProductId(wishlistId, productId);
        if (exists) {
            throw new RuntimeException("Sản phẩm đã có trong wishlist");
        }

        WishListDetail detail = new WishListDetail();
        detail.setWishlist(wishlist);
        detail.setProductId(productId);
        detail.setCreated_at(new Date());
        detail.setNote("");

        detailRepository.save(detail);
    }
}