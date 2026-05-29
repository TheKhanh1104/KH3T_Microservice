package fit.iuh.kh3tshopbe.saga.service;

import fit.iuh.kh3tshopbe.saga.event.OrderEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SagaCoordinator {

    public static final String ORDER_CREATED_TOPIC = "order.created";
    public static final String INVENTORY_RESERVED_TOPIC = "inventory.reserved";
    public static final String INVENTORY_FAILED_TOPIC = "inventory.failed";
    public static final String PAYMENT_REQUESTED_TOPIC = "payment.requested";
    public static final String PAYMENT_CHARGED_TOPIC = "payment.charged";
    public static final String PAYMENT_FAILED_TOPIC = "payment.failed";
    public static final String ORDER_SHIPPED_TOPIC = "order.shipped";
    public static final String ORDER_CANCELLED_TOPIC = "order.cancelled";

    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public void publish(String topic, OrderEvent event) {
        kafkaTemplate.send(topic, event.getOrderId().toString(), event);
    }
}