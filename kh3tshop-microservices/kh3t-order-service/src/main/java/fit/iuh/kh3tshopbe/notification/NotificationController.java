package fit.iuh.kh3tshopbe.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class NotificationController {

    private final OrderNotificationService service;

    // Support both dev (requests hitting service as /api/...) and gateway (rewritten to /orders/...)
    @GetMapping({"/api/orders/subscribe", "/orders/subscribe"})
    public SseEmitter subscribe(@RequestParam String userId) {
        UUID uuid;
        try {
            uuid = UUID.fromString(userId);
        } catch (IllegalArgumentException ex) {
            // fallback: derive same UUID used by saga creation (nameUUIDFromBytes of account id string)
            uuid = UUID.nameUUIDFromBytes(userId.getBytes());
        }
        return service.subscribe(uuid);
    }
}
