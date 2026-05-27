package fit.iuh.kh3tshopbe.mapper;

import fit.iuh.kh3tshopbe.dto.response.CartDetailResponse;
import fit.iuh.kh3tshopbe.entities.CartDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartDetailMapper {
    @Mapping(source = "id", target = "id")
    @Mapping(source = "productId", target = "productId")
    @Mapping(source = "price_at_time", target = "priceAtTime")
    @Mapping(source = "quantity", target = "quantity")
    @Mapping(source = "subtotal", target = "subtotal")
    @Mapping(source = "sizeDetailId", target = "sizeDetailId")
    CartDetailResponse toCartDetailResponse(CartDetail cartDetail);
}
