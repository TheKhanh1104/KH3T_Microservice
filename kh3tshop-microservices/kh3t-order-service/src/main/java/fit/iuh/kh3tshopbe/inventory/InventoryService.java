package fit.iuh.kh3tshopbe.inventory;

import fit.iuh.kh3tshopbe.saga.dto.OrderItemDTO;
import fit.iuh.kh3tshopbe.saga.event.OrderEvent;
import fit.iuh.kh3tshopbe.saga.service.SagaCoordinator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository repository;
    private final SagaCoordinator coordinator;
    private final ReservationRecordRepository reservationRecordRepository;

    @Transactional
    public boolean reserve(UUID orderId, UUID userId, List<OrderItemDTO> items) {
        // idempotent: if reservation record exists, do nothing
        if (reservationRecordRepository.existsById(orderId)) {
            return true;
        }

        for (OrderItemDTO it : items) {
            int updated = repository.reserve(it.getProductId(), it.getQuantity());
            if (updated == 0) {
                // publish inventory.failed for this order
                OrderEvent evt = OrderEvent.builder()
                        .eventType(SagaCoordinator.INVENTORY_FAILED_TOPIC)
                        .orderId(orderId)
                        .userId(userId)
                        .items(List.of(it))
                        .timestamp(LocalDateTime.now())
                        .build();
                coordinator.publish(SagaCoordinator.INVENTORY_FAILED_TOPIC, evt);
                return false;
            }
        }

        // persist reservation record so duplicate events are ignored
        reservationRecordRepository.save(ReservationRecord.builder()
                .orderId(orderId)
                .createdAt(LocalDateTime.now())
                .build());

        // all reserved
        OrderEvent evt = OrderEvent.builder()
                .eventType(SagaCoordinator.INVENTORY_RESERVED_TOPIC)
                .orderId(orderId)
                .userId(userId)
                .items(items)
                .timestamp(LocalDateTime.now())
                .build();
        coordinator.publish(SagaCoordinator.INVENTORY_RESERVED_TOPIC, evt);
        return true;
    }

    @Transactional
    public void confirm(UUID orderId, UUID userId, List<OrderItemDTO> items) {
        for (OrderItemDTO it : items) {
            repository.confirm(it.getProductId(), it.getQuantity());
        }
        OrderEvent evt = OrderEvent.builder()
                .eventType(SagaCoordinator.INVENTORY_CONFIRM_TOPIC)
                .orderId(orderId)
                .userId(userId)
                .items(items)
                .timestamp(LocalDateTime.now())
                .build();
        coordinator.publish(SagaCoordinator.INVENTORY_CONFIRM_TOPIC, evt);
    }

    @Transactional
    public void release(UUID orderId, UUID userId, List<OrderItemDTO> items) {
        for (OrderItemDTO it : items) {
            repository.release(it.getProductId(), it.getQuantity());
        }
        OrderEvent evt = OrderEvent.builder()
                .eventType(SagaCoordinator.INVENTORY_RELEASE_TOPIC)
                .orderId(orderId)
                .userId(userId)
                .items(items)
                .timestamp(LocalDateTime.now())
                .build();
        coordinator.publish(SagaCoordinator.INVENTORY_RELEASE_TOPIC, evt);
    }
}
