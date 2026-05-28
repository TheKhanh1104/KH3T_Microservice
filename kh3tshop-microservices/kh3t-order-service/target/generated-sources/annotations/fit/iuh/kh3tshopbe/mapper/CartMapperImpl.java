package fit.iuh.kh3tshopbe.mapper;

import fit.iuh.kh3tshopbe.dto.response.CartResponse;
import fit.iuh.kh3tshopbe.entities.Cart;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class CartMapperImpl implements CartMapper {

    @Override
    public CartResponse toCartResponse(Cart cart) {
        if ( cart == null ) {
            return null;
        }

        CartResponse.CartResponseBuilder cartResponse = CartResponse.builder();

        cartResponse.id( cart.getId() );
        cartResponse.totalAmount( cart.getTotalAmount() );
        cartResponse.totalQuantity( cart.getTotalQuantity() );

        return cartResponse.build();
    }
}
