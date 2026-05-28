package fit.iuh.kh3tshopbe.saga.messaging;

import fit.iuh.kh3tshopbe.saga.event.OrderEvent;
import fit.iuh.kh3tshopbe.saga.service.SagaCoordinator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class MockInventoryConsumer {

    private final SagaCoordinator coordinator;

    @KafkaListener(topics = SagaCoordinator.ORDER_CREATED_TOPIC, groupId = "order-service")
    public void onOrderCreated(OrderEvent event) throws InterruptedException {
        Thread.sleep(500);
        coordinator.publish(SagaCoordinator.INVENTORY_RESERVED_TOPIC, OrderEvent.builder()
                .eventType("INVENTORY_RESERVED")
                .orderId(event.getOrderId())
                .userId(event.getUserId())
                .totalAmount(event.getTotalAmount())
                .items(event.getItems())
                .timestamp(LocalDateTime.now())
                .build());
    }

    @KafkaListener(topics = SagaCoordinator.ORDER_CANCELLED_TOPIC, groupId = "order-service")
    public void onOrderCancelled(OrderEvent event) throws InterruptedException {
        Thread.sleep(200);
        log.info("Trả lại tồn kho cho orderId: {}", event.getOrderId());
    }
}