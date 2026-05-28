package fit.iuh.kh3tshopbe.saga.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDTO {
    private UUID id;

    @NotBlank
    private String productId;

    @NotBlank
    private String productName;

    @Min(1)
    private int quantity;

    @Min(0)
    private double unitPrice;

    @NotNull
    private String size;
}