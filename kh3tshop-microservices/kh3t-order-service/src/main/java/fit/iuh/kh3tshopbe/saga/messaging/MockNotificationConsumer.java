package fit.iuh.kh3tshopbe.saga.messaging;

import fit.iuh.kh3tshopbe.saga.event.OrderEvent;
import fit.iuh.kh3tshopbe.saga.service.SagaCoordinator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MockNotificationConsumer {

    @KafkaListener(topics = SagaCoordinator.ORDER_SHIPPED_TOPIC, groupId = "order-service")
    public void onOrderShipped(OrderEvent event) {
        log.info("Gửi thông báo giao hàng cho orderId: {}", event.getOrderId());
    }
}