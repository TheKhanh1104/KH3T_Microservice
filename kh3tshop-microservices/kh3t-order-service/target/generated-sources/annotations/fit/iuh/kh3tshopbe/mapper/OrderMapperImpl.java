package fit.iuh.kh3tshopbe.mapper;

import fit.iuh.kh3tshopbe.dto.response.CustomerTradingResponse;
import fit.iuh.kh3tshopbe.dto.response.OrderDetailResponse;
import fit.iuh.kh3tshopbe.dto.response.OrderResponse;
import fit.iuh.kh3tshopbe.entities.CustomerTrading;
import fit.iuh.kh3tshopbe.entities.Order;
import fit.iuh.kh3tshopbe.entities.OrderDetail;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class OrderMapperImpl implements OrderMapper {

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Override
    public OrderResponse toOrderMapper(Order order) {
        if ( order == null ) {
            return null;
        }

        OrderResponse.OrderResponseBuilder orderResponse = OrderResponse.builder();

        orderResponse.paymentMethod( order.getPaymentMethod() );
        orderResponse.orderDetails( orderDetailListToOrderDetailResponseList( order.getOrderDetails() ) );
        orderResponse.customerTrading( customerTradingToCustomerTradingResponse( order.getCustomerTrading() ) );
        orderResponse.id( order.getId() );
        orderResponse.note( order.getNote() );
        orderResponse.orderCode( order.getOrderCode() );
        orderResponse.orderDate( order.getOrderDate() );
        orderResponse.statusOrder( order.getStatusOrder() );

        return orderResponse.build();
    }

    protected List<OrderDetailResponse> orderDetailListToOrderDetailResponseList(List<OrderDetail> list) {
        if ( list == null ) {
            return null;
        }

        List<OrderDetailResponse> list1 = new ArrayList<OrderDetailResponse>( list.size() );
        for ( OrderDetail orderDetail : list ) {
            list1.add( orderDetailMapper.toOrderDetailResponse( orderDetail ) );
        }

        return list1;
    }

    protected CustomerTradingResponse customerTradingToCustomerTradingResponse(CustomerTrading customerTrading) {
        if ( customerTrading == null ) {
            return null;
        }

        CustomerTradingResponse.CustomerTradingResponseBuilder customerTradingResponse = CustomerTradingResponse.builder();

        customerTradingResponse.id( customerTrading.getId() );
        customerTradingResponse.receiverAddress( customerTrading.getReceiverAddress() );
        customerTradingResponse.receiverEmail( customerTrading.getReceiverEmail() );
        customerTradingResponse.receiverName( customerTrading.getReceiverName() );
        customerTradingResponse.receiverPhone( customerTrading.getReceiverPhone() );
        customerTradingResponse.totalAmount( customerTrading.getTotalAmount() );

        return customerTradingResponse.build();
    }
}
