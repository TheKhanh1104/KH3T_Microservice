package fit.iuh.kh3tshopbe.service;

import fit.iuh.kh3tshopbe.client.CatalogClient;
import fit.iuh.kh3tshopbe.dto.request.OrderDetailRequest;
import fit.iuh.kh3tshopbe.dto.response.ApiResponse;
import fit.iuh.kh3tshopbe.dto.response.OrderDetailResponse;
import fit.iuh.kh3tshopbe.dto.response.ProductResponse;
import fit.iuh.kh3tshopbe.entities.Order;
import fit.iuh.kh3tshopbe.entities.OrderDetail;
import fit.iuh.kh3tshopbe.mapper.OrderDetailMapper;
import fit.iuh.kh3tshopbe.repository.OrderDetailRepository;
import fit.iuh.kh3tshopbe.repository.OrderRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class OrderDetailService {
    OrderDetailRepository orderDetailRepository;
    CatalogClient catalogClient;
    OrderRepository orderRepository;
    OrderDetailMapper orderDetailMapper;

    public OrderDetailResponse createOrderDetail(OrderDetailRequest orderDetailRequest) {
        ApiResponse<ProductResponse> productRes = catalogClient.getProductById(orderDetailRequest.getProductId());
        if (productRes == null || productRes.getResult() == null) {
            throw new RuntimeException("Product not found");
        }

        Order order = orderRepository.findById(orderDetailRequest.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setProductId(orderDetailRequest.getProductId());
        orderDetail.setOrder(order);
        orderDetail.setProductName(orderDetailRequest.getProductName());
        orderDetail.setQuantity(orderDetailRequest.getQuantity());
        orderDetail.setCreated_at(new Date());
        orderDetail.setUpdated_at(new Date());
        orderDetail.setTotalPrice(orderDetailRequest.getTotalPrice());
        orderDetail.setUnitPrice(orderDetailRequest.getUnitPrice());

        OrderDetail saved = orderDetailRepository.save(orderDetail);
        return orderDetailMapper.toOrderDetailResponse(saved);
    }
}
