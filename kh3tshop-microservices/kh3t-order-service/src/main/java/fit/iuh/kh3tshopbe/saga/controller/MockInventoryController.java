package fit.iuh.kh3tshopbe.saga.controller;

import fit.iuh.kh3tshopbe.saga.event.OrderEvent;
import fit.iuh.kh3tshopbe.saga.service.SagaCoordinator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/mock/inventory")
@RequiredArgsConstructor
@Slf4j
public class MockInventoryController {

    private final SagaCoordinator coordinator;

    @PostMapping("/reserve")
    public ResponseEntity<String> reserve(@RequestBody OrderEvent event) throws InterruptedException {
        // simulate processing delay
        Thread.sleep(300);
        log.info("MockInventory: reserving inventory for order {}", event.getOrderId());

        OrderEvent reply = OrderEvent.builder()
                .eventType("INVENTORY_RESERVED")
                .orderId(event.getOrderId())
                .userId(event.getUserId())
                .totalAmount(event.getTotalAmount())
                .items(event.getItems())
                .timestamp(LocalDateTime.now())
                .build();

        coordinator.publish(SagaCoordinator.INVENTORY_RESERVED_TOPIC, reply);
        return ResponseEntity.ok("reserved");
    }

    @PostMapping("/release")
    public ResponseEntity<String> release(@RequestBody OrderEvent event) throws InterruptedException {
        Thread.sleep(200);
        log.info("MockInventory: releasing inventory for order {}", event.getOrderId());

        OrderEvent reply = OrderEvent.builder()
                .eventType("INVENTORY_RELEASED")
                .orderId(event.getOrderId())
                .userId(event.getUserId())
                .totalAmount(event.getTotalAmount())
                .items(event.getItems())
                .timestamp(LocalDateTime.now())
                .build();

        coordinator.publish(SagaCoordinator.INVENTORY_RELEASE_TOPIC, reply);
        return ResponseEntity.ok("released");
    }

    @PostMapping("/confirm")
    public ResponseEntity<String> confirm(@RequestBody OrderEvent event) throws InterruptedException {
        Thread.sleep(200);
        log.info("MockInventory: confirm inventory for order {}", event.getOrderId());

        OrderEvent reply = OrderEvent.builder()
                .eventType("INVENTORY_CONFIRMED")
                .orderId(event.getOrderId())
                .userId(event.getUserId())
                .totalAmount(event.getTotalAmount())
                .items(event.getItems())
                .timestamp(LocalDateTime.now())
                .build();

        coordinator.publish(SagaCoordinator.INVENTORY_CONFIRM_TOPIC, reply);
        return ResponseEntity.ok("confirmed");
    }
}
