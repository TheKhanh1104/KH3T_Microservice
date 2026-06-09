package fit.iuh.kh3tshopbe.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import fit.iuh.kh3tshopbe.saga.event.OrderEvent;
import fit.iuh.kh3tshopbe.saga.service.SagaCoordinator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxRelayScheduler {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final org.springframework.context.ApplicationContext applicationContext;

    @Scheduled(fixedDelay = 3000)
    public void relayOutbox() {
        List<OutboxEvent> events = outboxRepository.findTop100BySentFalseOrderByCreatedAtAsc();
        if (events.isEmpty()) return;

        for (OutboxEvent e : events) {
            boolean sentSuccess = false;
            OrderEvent event = null;
            try {
                event = objectMapper.readValue(e.getPayload(), OrderEvent.class);
                // Cố gắng gửi Kafka với timeout tối đa 1 giây để tránh nghẽn pool vĩnh viễn
                kafkaTemplate.send(e.getEventType(), e.getId().toString(), e.getPayload())
                             .get(1, TimeUnit.SECONDS);
                sentSuccess = true;
                log.info("Successfully published event {} via Kafka", e.getId());
            } catch (Exception ex) {
                log.warn("Failed to publish outbox event via Kafka {}: {}. Falling back to in-memory processing.", e.getId(), ex.getMessage());
            }

            // Fallback xử lý trực tiếp trong bộ nhớ (In-Memory) để đảm bảo chuỗi Saga vẫn chạy trên Render không có Kafka
            if (!sentSuccess && event != null) {
                try {
                    fallbackProcessInMemory(e.getEventType(), event);
                } catch (Exception fallbackEx) {
                    log.error("Failed in-memory fallback for event {}: {}", e.getId(), fallbackEx.getMessage());
                }
            }

            // Đánh dấu đã gửi (sent = true) trong transaction nhỏ riêng biệt để không hold connection pool
            try {
                updateOutboxSent(e.getId());
            } catch (Exception saveEx) {
                log.error("Failed to update outbox status for event {}: {}", e.getId(), saveEx.getMessage());
            }
        }
    }

    @Transactional
    public void updateOutboxSent(java.util.UUID id) {
        outboxRepository.findById(id).ifPresent(e -> {
            e.setSent(true);
            outboxRepository.save(e);
        });
    }

    private void fallbackProcessInMemory(String topic, OrderEvent event) {
        log.info("Processing event in-memory for topic: {}", topic);
        try {
            if (SagaCoordinator.ORDER_CREATED_TOPIC.equals(topic)) {
                var consumer = applicationContext.getBean(fit.iuh.kh3tshopbe.saga.messaging.MockInventoryConsumer.class);
                consumer.onOrderCreated(event);
            } else if (SagaCoordinator.PAYMENT_REQUESTED_TOPIC.equals(topic)) {
                var consumer = applicationContext.getBean(fit.iuh.kh3tshopbe.saga.messaging.MockPaymentConsumer.class);
                consumer.onPaymentRequested(event);
            } else if (SagaCoordinator.INVENTORY_RESERVED_TOPIC.equals(topic)) {
                var listener = applicationContext.getBean(fit.iuh.kh3tshopbe.saga.messaging.SagaEventListener.class);
                listener.onInventoryReserved(event);
            } else if (SagaCoordinator.PAYMENT_CHARGED_TOPIC.equals(topic)) {
                var listener = applicationContext.getBean(fit.iuh.kh3tshopbe.saga.messaging.SagaEventListener.class);
                listener.onPaymentCharged(event);
            } else if (SagaCoordinator.PAYMENT_FAILED_TOPIC.equals(topic)) {
                var listener = applicationContext.getBean(fit.iuh.kh3tshopbe.saga.messaging.SagaEventListener.class);
                listener.onPaymentFailed(event);
            } else if (SagaCoordinator.INVENTORY_FAILED_TOPIC.equals(topic)) {
                var listener = applicationContext.getBean(fit.iuh.kh3tshopbe.saga.messaging.SagaEventListener.class);
                listener.onInventoryFailed(event);
            } else if (SagaCoordinator.ORDER_CANCELLED_TOPIC.equals(topic)) {
                var invConsumer = applicationContext.getBean(fit.iuh.kh3tshopbe.saga.messaging.MockInventoryConsumer.class);
                invConsumer.onOrderCancelled(event);
                var payConsumer = applicationContext.getBean(fit.iuh.kh3tshopbe.saga.messaging.MockPaymentConsumer.class);
                payConsumer.onOrderCancelled(event);
            } else if (SagaCoordinator.INVENTORY_RELEASE_TOPIC.equals(topic)) {
                var service = applicationContext.getBean(fit.iuh.kh3tshopbe.inventory.InventoryService.class);
                service.release(event.getOrderId(), event.getUserId(), event.getItems());
            }
        } catch (Exception ex) {
            log.error("Error executing in-memory fallback for topic {}: {}", topic, ex.getMessage(), ex);
        }
    }
}
