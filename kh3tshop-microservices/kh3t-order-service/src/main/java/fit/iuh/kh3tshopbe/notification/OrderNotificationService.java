package fit.iuh.kh3tshopbe.notification;

import fit.iuh.kh3tshopbe.saga.dto.OrderResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class OrderNotificationService {

    private final Map<UUID, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(UUID userId) {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.put(userId, emitter);
        emitter.onCompletion(() -> emitters.remove(userId));
        emitter.onTimeout(() -> emitters.remove(userId));
        return emitter;
    }

    public void sendUpdate(UUID userId, OrderResponse update) {
        SseEmitter emitter = emitters.get(userId);
        if (emitter == null) return;
        try {
            emitter.send(SseEmitter.event().name("order.update").data(update));
        } catch (IOException e) {
            log.warn("Failed to send SSE to {}: {}", userId, e.getMessage());
            emitters.remove(userId);
        }
    }
}
