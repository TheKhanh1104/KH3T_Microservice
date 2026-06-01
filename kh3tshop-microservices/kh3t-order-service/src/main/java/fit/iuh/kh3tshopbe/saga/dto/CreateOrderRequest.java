package fit.iuh.kh3tshopbe.saga.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {
    @NotNull
    private UUID userId;

    private Integer customerTradingId;

    @NotEmpty
    @Valid
    private List<OrderItemDTO> items;
    // Optional token used to match external payment callbacks
    private String paymentToken;
}