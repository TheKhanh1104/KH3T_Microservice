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
    public OrderResponse createOrder(@RequestBody OrderRequest orderRequest) throws ParseException {
        return orderService.createOrder(orderRequest);
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
