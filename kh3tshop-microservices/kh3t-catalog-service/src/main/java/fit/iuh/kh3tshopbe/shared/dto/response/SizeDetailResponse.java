package fit.iuh.kh3tshopbe.shared.dto.response;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class SizeDetailResponse {
    private int id;
    private int quantity;
    private int sizeId;
}
