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
public class MockPaymentConsumer {

    private final SagaCoordinator coordinator;

    @KafkaListener(topics = SagaCoordinator.PAYMENT_REQUESTED_TOPIC, groupId = "order-service")
    public void onPaymentRequested(OrderEvent event) throws InterruptedException {
        Thread.sleep(300);
        coordinator.publish(SagaCoordinator.PAYMENT_CHARGED_TOPIC, OrderEvent.builder()
                .eventType("PAYMENT_CHARGED")
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
        log.info("Hoàn tiền cho orderId: {}", event.getOrderId());
    }
}