package fit.iuh.kh3tshopbe.mapper;

import fit.iuh.kh3tshopbe.dto.response.OrderDetailResponse;
import fit.iuh.kh3tshopbe.entities.OrderDetail;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Oracle Corporation)"
)
@Component
public class OrderDetailMapperImpl implements OrderDetailMapper {

    @Override
    public OrderDetailResponse toOrderDetailResponse(OrderDetail orderDetail) {
        if ( orderDetail == null ) {
            return null;
        }

        OrderDetailResponse.OrderDetailResponseBuilder orderDetailResponse = OrderDetailResponse.builder();

        orderDetailResponse.productId( orderDetail.getProductId() );
        orderDetailResponse.productName( orderDetail.getProductName() );
        orderDetailResponse.quantity( orderDetail.getQuantity() );
        orderDetailResponse.unitPrice( orderDetail.getUnitPrice() );
        orderDetailResponse.totalPrice( orderDetail.getTotalPrice() );

        orderDetailResponse.orderId( orderDetail.getOrder() != null ? orderDetail.getOrder().getId() : 0 );

        return orderDetailResponse.build();
    }
}
