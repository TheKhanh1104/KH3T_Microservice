package fit.iuh.kh3tshopbe.shared.dto.response;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CategoryResponse {
    private int id;
    private String name;
    private String imageUrl;
}
