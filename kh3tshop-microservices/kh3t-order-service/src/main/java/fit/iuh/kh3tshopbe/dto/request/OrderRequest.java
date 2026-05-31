package fit.iuh.kh3tshopbe.dto.request;

import fit.iuh.kh3tshopbe.enums.PaymentMethod;
import fit.iuh.kh3tshopbe.saga.dto.OrderItemDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {
    private String note;
    private int customerTradingId;
    private int account_id;
    private PaymentMethod paymentMethod;
    private List<OrderItemDTO> items;
}
