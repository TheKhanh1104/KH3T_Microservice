package fit.iuh.kh3tshopbe.saga.event;

import fit.iuh.kh3tshopbe.saga.dto.OrderItemDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEvent {
    private String eventType;
    private UUID orderId;
    private UUID userId;
    private double totalAmount;
    private List<OrderItemDTO> items;
    private LocalDateTime timestamp;
}