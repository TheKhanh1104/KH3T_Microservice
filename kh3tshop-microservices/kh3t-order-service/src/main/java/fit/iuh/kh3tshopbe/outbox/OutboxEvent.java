package fit.iuh.kh3tshopbe.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEvent {
    @Id
    private UUID id;

    @Column(name = "event_type")
    private String eventType;

    @Column(columnDefinition = "text")
    private String payload;

    private boolean sent;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
