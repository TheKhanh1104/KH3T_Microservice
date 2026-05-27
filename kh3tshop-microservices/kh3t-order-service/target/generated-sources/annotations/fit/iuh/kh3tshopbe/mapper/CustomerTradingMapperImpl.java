package fit.iuh.kh3tshopbe.mapper;

import fit.iuh.kh3tshopbe.dto.response.CustomerTradingResponse;
import fit.iuh.kh3tshopbe.entities.CustomerTrading;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.4 (Oracle Corporation)"
)
@Component
public class CustomerTradingMapperImpl implements CustomerTradingMapper {

    @Override
    public CustomerTradingResponse toCustomerTradingMapper(CustomerTrading customerTrading) {
        if ( customerTrading == null ) {
            return null;
        }

        CustomerTradingResponse.CustomerTradingResponseBuilder customerTradingResponse = CustomerTradingResponse.builder();

        customerTradingResponse.id( customerTrading.getId() );
        customerTradingResponse.receiverName( customerTrading.getReceiverName() );
        customerTradingResponse.receiverPhone( customerTrading.getReceiverPhone() );
        customerTradingResponse.receiverEmail( customerTrading.getReceiverEmail() );
        customerTradingResponse.receiverAddress( customerTrading.getReceiverAddress() );
        customerTradingResponse.totalAmount( customerTrading.getTotalAmount() );

        return customerTradingResponse.build();
    }
}
