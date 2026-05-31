package fit.iuh.kh3tshopbe.controller;

import fit.iuh.kh3tshopbe.dto.request.OrderRequest;
import fit.iuh.kh3tshopbe.dto.request.UpdateOrderStatusRequest;
import fit.iuh.kh3tshopbe.dto.response.*;
import fit.iuh.kh3tshopbe.enums.StatusOrdering;
import fit.iuh.kh3tshopbe.saga.dto.CreateOrderRequest;
import fit.iuh.kh3tshopbe.saga.service.SagaOrderService;
import fit.iuh.kh3tshopbe.service.OrderService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderController {
    OrderService orderService;
    SagaOrderService sagaOrderService;
    fit.iuh.kh3tshopbe.service.CartService cartService;
    fit.iuh.kh3tshopbe.service.CustomerTradingService customerTradingService;

    @GetMapping
    public List<OrderResponse> getAllOrders() {
        return orderService.getAllOrders();
    }

    @PostMapping
    public ResponseEntity<fit.iuh.kh3tshopbe.saga.dto.OrderResponse> createSagaOrder(@RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sagaOrderService.createOrder(request));
    }

    @GetMapping("/saga")
    public ResponseEntity<List<fit.iuh.kh3tshopbe.saga.dto.OrderResponse>> getSagaOrders() {
        return ResponseEntity.ok(sagaOrderService.getAllOrders());
    }

    @GetMapping("/saga/{id}")
    public ResponseEntity<fit.iuh.kh3tshopbe.saga.dto.OrderResponse> getSagaOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(sagaOrderService.getOrderById(id));
    }

    @PutMapping("/saga/{id}/confirm")
    public ResponseEntity<fit.iuh.kh3tshopbe.saga.dto.OrderResponse> confirmSagaOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(sagaOrderService.confirmOrder(id));
    }

    @GetMapping("/saga/user/{userId}")
    public ResponseEntity<List<fit.iuh.kh3tshopbe.saga.dto.OrderResponse>> getSagaOrdersByUser(@PathVariable String userId) {
        UUID uuid;
        try {
            uuid = UUID.fromString(userId);
        } catch (IllegalArgumentException ex) {
            uuid = UUID.nameUUIDFromBytes(userId.getBytes());
        }
        return ResponseEntity.ok(sagaOrderService.getOrdersByUserId(uuid));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<fit.iuh.kh3tshopbe.saga.dto.OrderResponse> cancelSagaOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(sagaOrderService.cancelOrder(id));
    }

    @PutMapping("/{id}/ship")
    public ResponseEntity<fit.iuh.kh3tshopbe.saga.dto.OrderResponse> shipSagaOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(sagaOrderService.shipOrder(id));
    }

    @PutMapping("/{id}/deliver")
    public ResponseEntity<fit.iuh.kh3tshopbe.saga.dto.OrderResponse> deliverSagaOrder(@PathVariable UUID id) {
        return ResponseEntity.ok(sagaOrderService.deliverOrder(id));
    }

        @PostMapping("/create")
        public ResponseEntity<fit.iuh.kh3tshopbe.saga.dto.OrderResponse> createOrder(@RequestBody OrderRequest orderRequest) throws ParseException {
        var customerTrading = customerTradingService.getCustomerTradingById(orderRequest.getCustomerTradingId());
            // Prefer items provided by FE; fall back to cart contents if not present.
            var requestItems = orderRequest.getItems();
            var items = (requestItems != null && !requestItems.isEmpty())
                ? requestItems.stream()
                    .map(i -> fit.iuh.kh3tshopbe.saga.dto.OrderItemDTO.builder()
                        .productId(i.getProductId())
                        .productName(i.getProductName())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .size(i.getSize())
                        .build())
                    .toList()
                : java.util.Optional.ofNullable(cartService.getCartByAccountId(orderRequest.getAccount_id()).getCart_details())
                    .orElse(java.util.List.of())
                    .stream()
                    .filter(f -> f.isSelected())
                    .map(cd -> fit.iuh.kh3tshopbe.saga.dto.OrderItemDTO.builder()
                        .productId(String.valueOf(cd.getProductId()))
                        .productName("") // product name can be enriched by catalog service
                        .quantity(cd.getQuantity())
                        .unitPrice(cd.getPrice_at_time())
                        .size(String.valueOf(cd.getSizeDetailId()))
                        .build())
                    .toList();

        java.util.UUID userUuid = java.util.UUID.nameUUIDFromBytes(String.valueOf(orderRequest.getAccount_id()).getBytes());
        var sagaReq = fit.iuh.kh3tshopbe.saga.dto.CreateOrderRequest.builder()
            .userId(userUuid)
            .customerTradingId(orderRequest.getCustomerTradingId())
            .items(items)
            .build();

        var resp = sagaOrderService.createOrder(sagaReq);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        }

    @PutMapping("/status/{id}")
    public OrderResponse updateOrderStatus(@PathVariable int id, @RequestBody UpdateOrderStatusRequest request) {
        return orderService.updateOrderStatus(id, request.getStatusOrder());
    }

    @GetMapping("/detailed-orders")
    public List<DetailedOrderResponse> getAllDetailedOrders() {
        return orderService.getDetailedOrders();
    }

    @GetMapping("/time-slots")
    public List<TimeSlotStatisticResponse> getTimeSlots() {
        return orderService.getTimeSlotStats();
    }
    @GetMapping("/daily")
    public List<DailyStatisticResponse> getDailyStats(
            @RequestParam String start,
            @RequestParam String end) {
        LocalDate startDate = LocalDate.parse(start);
        LocalDate endDate = LocalDate.parse(end);

        LocalDateTime startDateTime = startDate.atStartOfDay(); // 2025-11-10 00:00:00
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59); // 2025-11-10 23:59:59

        return orderService.getDailyStats(startDateTime, endDateTime);
    }


    @GetMapping("account/{account_id}")
    public List<OrderResponse> getOrderByAccountId(@PathVariable int account_id){
        return orderService.getOrdersByAccountId(account_id);
    }

}
