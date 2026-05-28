package fit.iuh.kh3tshopbe.mapper;

import fit.iuh.kh3tshopbe.dto.response.CartDetailResponse;
import fit.iuh.kh3tshopbe.entities.CartDetail;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Oracle Corporation)"
)
@Component
public class CartDetailMapperImpl implements CartDetailMapper {

    @Override
    public CartDetailResponse toCartDetailResponse(CartDetail cartDetail) {
        if ( cartDetail == null ) {
            return null;
        }

        CartDetailResponse.CartDetailResponseBuilder cartDetailResponse = CartDetailResponse.builder();

        cartDetailResponse.id( cartDetail.getId() );
        cartDetailResponse.productId( cartDetail.getProductId() );
        cartDetailResponse.priceAtTime( cartDetail.getPrice_at_time() );
        cartDetailResponse.quantity( cartDetail.getQuantity() );
        cartDetailResponse.subtotal( cartDetail.getSubtotal() );
        cartDetailResponse.sizeDetailId( cartDetail.getSizeDetailId() );

        return cartDetailResponse.build();
    }
}
