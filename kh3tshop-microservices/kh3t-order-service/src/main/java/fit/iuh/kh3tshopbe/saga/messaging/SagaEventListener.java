package fit.iuh.kh3tshopbe.saga.messaging;

import fit.iuh.kh3tshopbe.saga.event.OrderEvent;
import fit.iuh.kh3tshopbe.saga.service.SagaCoordinator;
import fit.iuh.kh3tshopbe.saga.service.SagaOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SagaEventListener {

    private final SagaOrderService sagaOrderService;

    @KafkaListener(topics = SagaCoordinator.INVENTORY_RESERVED_TOPIC, groupId = "order-service")
    public void onInventoryReserved(OrderEvent event) {
        sagaOrderService.onInventoryReserved(event.getOrderId());
    }

    @KafkaListener(topics = SagaCoordinator.PAYMENT_CHARGED_TOPIC, groupId = "order-service")
    public void onPaymentCharged(OrderEvent event) {
        sagaOrderService.onPaymentCharged(event.getOrderId());
    }

    @KafkaListener(topics = SagaCoordinator.PAYMENT_FAILED_TOPIC, groupId = "order-service")
    public void onPaymentFailed(OrderEvent event) {
        sagaOrderService.cancelOrder(event.getOrderId());
    }

    @KafkaListener(topics = SagaCoordinator.INVENTORY_FAILED_TOPIC, groupId = "order-service")
    public void onInventoryFailed(OrderEvent event) {
        sagaOrderService.cancelOrder(event.getOrderId());
    }
}