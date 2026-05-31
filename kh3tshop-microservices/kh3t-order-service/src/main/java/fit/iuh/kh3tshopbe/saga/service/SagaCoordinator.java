package fit.iuh.kh3tshopbe.saga.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import fit.iuh.kh3tshopbe.outbox.OutboxEvent;
import fit.iuh.kh3tshopbe.outbox.OutboxRepository;
import fit.iuh.kh3tshopbe.saga.event.OrderEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SagaCoordinator {

    public static final String ORDER_CREATED_TOPIC = "order.created";
    public static final String INVENTORY_RESERVED_TOPIC = "inventory.reserved";
    public static final String INVENTORY_FAILED_TOPIC = "inventory.failed";
    public static final String PAYMENT_REQUESTED_TOPIC = "payment.requested";
    public static final String PAYMENT_CHARGED_TOPIC = "payment.charged";
    public static final String PAYMENT_FAILED_TOPIC = "payment.failed";
    public static final String INVENTORY_RELEASE_TOPIC = "inventory.release";
    public static final String INVENTORY_CONFIRM_TOPIC = "inventory.confirm";
    public static final String ORDER_SHIPPED_TOPIC = "order.shipped";
    public static final String ORDER_CANCELLED_TOPIC = "order.cancelled";

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public void publish(String topic, OrderEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            OutboxEvent out = OutboxEvent.builder()
                    .id(UUID.randomUUID())
                    .eventType(topic)
                    .payload(payload)
                    .sent(false)
                    .createdAt(LocalDateTime.now())
                    .build();
            outboxRepository.save(out);
        } catch (Exception e) {
            // log and swallow - outbox publish should not break transaction
            e.printStackTrace();
        }
    }
}