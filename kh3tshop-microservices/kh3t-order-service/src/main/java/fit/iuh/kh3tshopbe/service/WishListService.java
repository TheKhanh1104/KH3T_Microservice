package fit.iuh.kh3tshopbe.service;

import fit.iuh.kh3tshopbe.client.IdentityClient;
import fit.iuh.kh3tshopbe.dto.response.AccountResponse;
import fit.iuh.kh3tshopbe.dto.response.ApiResponse;
import fit.iuh.kh3tshopbe.dto.response.WishListResponse;
import fit.iuh.kh3tshopbe.entities.WishList;
import fit.iuh.kh3tshopbe.repository.WishListRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WishListService {

    WishListRepository wishlistRepository;
    IdentityClient identityClient;

    private int getAccountIdFromUsername(String username) {
        ApiResponse<AccountResponse> accountRes = identityClient.getAccountByUsername(username);
        if (accountRes == null || accountRes.getResult() == null) {
            throw new RuntimeException("User not found");
        }
        return accountRes.getResult().getId();
    }

    public WishListResponse createWishlist(WishList wishlist, String username) {
        int accountId = getAccountIdFromUsername(username);
        wishlist.setAccountId(accountId);
        wishlist.setCreated_at(new Date());
        wishlist.setUpdated_at(new Date());

        WishList saved = wishlistRepository.save(wishlist);
        return toResponse(saved, username);
    }

    public List<WishListResponse> getWishlistsByCurrentUser(String username) {
        int accountId = getAccountIdFromUsername(username);
        return wishlistRepository.findByAccountId(accountId).stream()
                .map(w -> toResponse(w, username))
                .toList();
    }

    public WishListResponse updateWishlist(Integer id, WishList updated, String username) {
        int accountId = getAccountIdFromUsername(username);
        WishList existing = wishlistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Wishlist not found"));

        if (existing.getAccountId() != accountId) {
            throw new RuntimeException("Bạn không có quyền sửa wishlist này");
        }

        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setUpdated_at(new Date());

        WishList saved = wishlistRepository.save(existing);
        return toResponse(saved, username);
    }

    public void deleteWishlist(Integer id, String username) {
        int accountId = getAccountIdFromUsername(username);
        WishList wishlist = wishlistRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Wishlist not found"));

        if (wishlist.getAccountId() != accountId) {
            throw new RuntimeException("Bạn không có quyền xóa wishlist này");
        }

        wishlistRepository.delete(wishlist);
    }

    public boolean isProductInWishlist(String username, Integer productId) {
        int accountId = getAccountIdFromUsername(username);
        return wishlistRepository.existsByAccountIdAndDetails_ProductId(accountId, productId);
    }

    private WishListResponse toResponse(WishList w, String username) {
        return WishListResponse.builder()
                .id(w.getId())
                .name(w.getName())
                .description(w.getDescription())
                .created_at(w.getCreated_at())
                .updated_at(w.getUpdated_at())
                .username(username)
                .itemCount(w.getDetails() != null ? w.getDetails().size() : 0)
                .build();
    }
}