package fit.iuh.kh3tshopbe.saga.messaging;

import fit.iuh.kh3tshopbe.saga.event.OrderEvent;
import fit.iuh.kh3tshopbe.saga.service.SagaCoordinator;
import fit.iuh.kh3tshopbe.inventory.InventoryService;
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
    private final InventoryService inventoryService;

    @KafkaListener(topics = SagaCoordinator.ORDER_CREATED_TOPIC, groupId = "order-service")
    public void onOrderCreated(OrderEvent event) throws InterruptedException {
        Thread.sleep(200);
        inventoryService.reserve(event.getOrderId(), event.getUserId(), event.getItems());
    }

    @KafkaListener(topics = SagaCoordinator.ORDER_CANCELLED_TOPIC, groupId = "order-service")
    public void onOrderCancelled(OrderEvent event) throws InterruptedException {
        Thread.sleep(200);
        log.info("Trả lại tồn kho cho orderId: {}", event.getOrderId());
    }
}