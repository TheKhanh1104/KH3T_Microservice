package fit.iuh.kh3tshopbe.mapper;

import fit.iuh.kh3tshopbe.dto.response.OrderDetailResponse;
import fit.iuh.kh3tshopbe.entities.OrderDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderDetailMapper {
    @Mapping(target = "orderId", expression = "java(orderDetail.getOrder() != null ? orderDetail.getOrder().getId() : 0)")
    @Mapping(source = "productId", target = "productId")
    OrderDetailResponse toOrderDetailResponse(OrderDetail orderDetail);
}
