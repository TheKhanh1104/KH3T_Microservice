package fit.iuh.kh3tshopbe.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxRelayScheduler {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelay = 3000)
    @Transactional
    public void relayOutbox() {
        List<OutboxEvent> events = outboxRepository.findTop100BySentFalseOrderByCreatedAtAsc();
        if (events.isEmpty()) return;

        for (OutboxEvent e : events) {
            try {
                // payload is already JSON string; send as value
                kafkaTemplate.send(e.getEventType(), e.getId().toString(), e.getPayload()).get();
                e.setSent(true);
                outboxRepository.save(e);
            } catch (Exception ex) {
                log.warn("Failed to publish outbox event {}: {}", e.getId(), ex.getMessage());
            }
        }
    }
}
