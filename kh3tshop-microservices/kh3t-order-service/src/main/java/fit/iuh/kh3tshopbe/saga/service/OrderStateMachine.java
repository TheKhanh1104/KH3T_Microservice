package fit.iuh.kh3tshopbe.saga.service;

import fit.iuh.kh3tshopbe.saga.domain.OrderStatus;
import fit.iuh.kh3tshopbe.saga.domain.SagaOrder;
import fit.iuh.kh3tshopbe.saga.exception.InvalidStateTransitionException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class OrderStateMachine {

    private static final Map<OrderStatus, List<OrderStatus>> ALLOWED = new EnumMap<>(OrderStatus.class);

    static {
        ALLOWED.put(OrderStatus.PENDING, List.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED, OrderStatus.FAILED));
        ALLOWED.put(OrderStatus.CONFIRMED, List.of(OrderStatus.PAID, OrderStatus.CANCELLED, OrderStatus.FAILED));
        ALLOWED.put(OrderStatus.PAID, List.of(OrderStatus.SHIPPING));
        ALLOWED.put(OrderStatus.SHIPPING, List.of(OrderStatus.DELIVERED));
    }

    public void transition(SagaOrder order, OrderStatus newStatus) {
        List<OrderStatus> allowed = ALLOWED.getOrDefault(order.getStatus(), List.of());
        if (!allowed.contains(newStatus)) {
            throw new InvalidStateTransitionException(order.getStatus(), newStatus);
        }
        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
    }
}