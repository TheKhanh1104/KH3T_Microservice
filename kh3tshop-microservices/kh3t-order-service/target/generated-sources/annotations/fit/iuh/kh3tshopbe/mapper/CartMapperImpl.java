package fit.iuh.kh3tshopbe.mapper;

import fit.iuh.kh3tshopbe.dto.response.CartResponse;
import fit.iuh.kh3tshopbe.entities.Cart;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Oracle Corporation)"
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
        cartResponse.totalQuantity( cart.getTotalQuantity() );
        cartResponse.totalAmount( cart.getTotalAmount() );

        return cartResponse.build();
    }
}
