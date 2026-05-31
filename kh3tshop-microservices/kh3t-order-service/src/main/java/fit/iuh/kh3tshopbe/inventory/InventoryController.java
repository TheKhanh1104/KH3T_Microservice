package fit.iuh.kh3tshopbe.inventory;

import fit.iuh.kh3tshopbe.saga.event.OrderEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService service;

    @PostMapping("/reserve")
    public ResponseEntity<?> reserve(@RequestBody OrderEvent evt) {
        boolean ok = service.reserve(evt.getOrderId(), evt.getUserId(), evt.getItems());
        if (ok) return ResponseEntity.ok().build();
        return ResponseEntity.status(409).body("insufficient inventory");
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirm(@RequestBody OrderEvent evt) {
        service.confirm(evt.getOrderId(), evt.getUserId(), evt.getItems());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/release")
    public ResponseEntity<?> release(@RequestBody OrderEvent evt) {
        service.release(evt.getOrderId(), evt.getUserId(), evt.getItems());
        return ResponseEntity.ok().build();
    }
}
