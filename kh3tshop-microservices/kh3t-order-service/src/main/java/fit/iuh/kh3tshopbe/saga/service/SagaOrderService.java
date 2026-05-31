package fit.iuh.kh3tshopbe.saga.service;

import fit.iuh.kh3tshopbe.saga.domain.OrderStatus;
import fit.iuh.kh3tshopbe.saga.domain.SagaOrder;
import fit.iuh.kh3tshopbe.saga.domain.SagaOrderItem;
import fit.iuh.kh3tshopbe.saga.dto.CreateOrderRequest;
import fit.iuh.kh3tshopbe.saga.dto.OrderItemDTO;
import fit.iuh.kh3tshopbe.saga.dto.OrderResponse;
import fit.iuh.kh3tshopbe.saga.event.OrderEvent;
import fit.iuh.kh3tshopbe.saga.event.OrderEventType;
import fit.iuh.kh3tshopbe.saga.exception.InvalidStateTransitionException;
import fit.iuh.kh3tshopbe.saga.exception.OrderNotFoundException;
import fit.iuh.kh3tshopbe.saga.repository.SagaOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SagaOrderService {

    private final SagaOrderRepository orderRepository;
    private final OrderStateMachine stateMachine;
    private final SagaCoordinator coordinator;
    private final fit.iuh.kh3tshopbe.notification.OrderNotificationService notificationService;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order items must not be empty");
        }

        SagaOrder order = SagaOrder.builder()
                .userId(request.getUserId())
                .status(OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        request.getItems().forEach(item -> order.addItem(toEntity(item)));
        order.setTotalAmount(calculateTotal(order.getItems()));

        SagaOrder saved = orderRepository.save(order);
        coordinator.publish(SagaCoordinator.ORDER_CREATED_TOPIC, toEvent(saved, OrderEventType.ORDER_CREATED));
        notificationService.sendUpdate(saved.getUserId(), toResponse(saved));
        return toResponse(saved);
    }

    @Transactional
    public OrderResponse cancelOrder(UUID orderId) {
        SagaOrder order = getRequiredOrder(orderId);
        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new InvalidStateTransitionException(order.getStatus(), OrderStatus.CANCELLED);
        }
        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        SagaOrder saved = orderRepository.save(order);
        coordinator.publish(SagaCoordinator.ORDER_CANCELLED_TOPIC, toEvent(saved, OrderEventType.ORDER_CANCELLED));
        notificationService.sendUpdate(saved.getUserId(), toResponse(saved));
        return toResponse(saved);
    }

    @Transactional
    public OrderResponse confirmOrder(UUID orderId) {
        SagaOrder order = getRequiredOrder(orderId);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidStateTransitionException(order.getStatus(), OrderStatus.CONFIRMED);
        }
        stateMachine.transition(order, OrderStatus.CONFIRMED);
        SagaOrder saved = orderRepository.save(order);
        coordinator.publish(SagaCoordinator.PAYMENT_REQUESTED_TOPIC, toEvent(saved, OrderEventType.PAYMENT_REQUESTED));
        notificationService.sendUpdate(saved.getUserId(), toResponse(saved));
        return toResponse(saved);
    }

    @Transactional
    public OrderResponse shipOrder(UUID orderId) {
        SagaOrder order = getRequiredOrder(orderId);
        stateMachine.transition(order, OrderStatus.SHIPPING);
        SagaOrder saved = orderRepository.save(order);
        coordinator.publish(SagaCoordinator.ORDER_SHIPPED_TOPIC, toEvent(saved, OrderEventType.ORDER_SHIPPED));
        notificationService.sendUpdate(saved.getUserId(), toResponse(saved));
        return toResponse(saved);
    }

    @Transactional
    public OrderResponse deliverOrder(UUID orderId) {
        SagaOrder order = getRequiredOrder(orderId);
        stateMachine.transition(order, OrderStatus.DELIVERED);
        SagaOrder saved = orderRepository.save(order);
        notificationService.sendUpdate(saved.getUserId(), toResponse(saved));
        return toResponse(saved);
    }

    @Transactional
    public void onInventoryReserved(UUID orderId) {
        SagaOrder order = getRequiredOrder(orderId);
        if (order.getStatus() != OrderStatus.PENDING) {
            return;
        }
        stateMachine.transition(order, OrderStatus.CONFIRMED);
        SagaOrder saved = orderRepository.save(order);
        coordinator.publish(SagaCoordinator.PAYMENT_REQUESTED_TOPIC, toEvent(saved, OrderEventType.PAYMENT_REQUESTED));
        notificationService.sendUpdate(saved.getUserId(), toResponse(saved));
    }

    @Transactional
    public void onPaymentCharged(UUID orderId) {
        SagaOrder order = getRequiredOrder(orderId);
        if (order.getStatus() == OrderStatus.PAID) {
            return;
        }
        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.PAID);
            order.setUpdatedAt(LocalDateTime.now());
            orderRepository.save(order);
            notificationService.sendUpdate(order.getUserId(), toResponse(order));
            return;
        }
        if (order.getStatus() != OrderStatus.CONFIRMED) {
            return;
        }
        stateMachine.transition(order, OrderStatus.PAID);
        orderRepository.save(order);
        notificationService.sendUpdate(order.getUserId(), toResponse(order));
    }

    @Transactional
    public void onPaymentConfirmed(UUID orderId) {
        SagaOrder order = getRequiredOrder(orderId);
        if (order.getStatus() == OrderStatus.CONFIRMED) {
            return;
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            return;
        }
        stateMachine.transition(order, OrderStatus.CONFIRMED);
        SagaOrder saved = orderRepository.save(order);
        notificationService.sendUpdate(saved.getUserId(), toResponse(saved));
    }

    @Transactional
    public void onPaymentFailed(UUID orderId) {
        SagaOrder order = getRequiredOrder(orderId);
        // Only handle if we were waiting for payment
        if (order.getStatus() != OrderStatus.CONFIRMED && order.getStatus() != OrderStatus.PENDING) {
            return;
        }
        // publish inventory release (compensation) and mark order failed
        coordinator.publish(SagaCoordinator.INVENTORY_RELEASE_TOPIC, toEvent(order, OrderEventType.PAYMENT_FAILED));
        stateMachine.transition(order, OrderStatus.FAILED);
        orderRepository.save(order);
        notificationService.sendUpdate(order.getUserId(), toResponse(order));
    }

    @Transactional
    public void onInventoryFailed(UUID orderId) {
        SagaOrder order = getRequiredOrder(orderId);
        if (order.getStatus() == OrderStatus.FAILED) return;
        stateMachine.transition(order, OrderStatus.FAILED);
        orderRepository.save(order);
        notificationService.sendUpdate(order.getUserId(), toResponse(order));
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(UUID orderId) {
        return toResponse(getRequiredOrder(orderId));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserId(UUID userId) {
        return orderRepository.findAllByUserIdOrderByCreatedAtDesc(userId).stream().map(this::toResponse).toList();
    }

    private SagaOrder getRequiredOrder(UUID orderId) {
        return orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    private SagaOrderItem toEntity(OrderItemDTO dto) {
        return SagaOrderItem.builder()
                .productId(dto.getProductId())
                .productName(dto.getProductName())
                .quantity(dto.getQuantity())
                .unitPrice(dto.getUnitPrice())
                .size(dto.getSize())
                .build();
    }

    private double calculateTotal(List<SagaOrderItem> items) {
        return items.stream().mapToDouble(item -> item.getUnitPrice() * item.getQuantity()).sum();
    }

    private OrderEvent toEvent(SagaOrder order, OrderEventType type) {
        return OrderEvent.builder()
                .eventType(type.name())
                .orderId(order.getId())
                .userId(order.getUserId())
                .totalAmount(order.getTotalAmount())
                .items(order.getItems().stream().map(this::toDto).toList())
                .timestamp(LocalDateTime.now())
                .build();
    }

    private OrderItemDTO toDto(SagaOrderItem item) {
        return OrderItemDTO.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .size(item.getSize())
                .build();
    }

    private OrderResponse toResponse(SagaOrder order) {
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(order.getItems().stream().map(this::toDto).toList())
                .paymentToken(null)
                .build();
    }

    public java.util.UUID findOrderIdByPaymentToken(String token) {
        return null;
    }
}