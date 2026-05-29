package fit.iuh.kh3tshopbe.shared.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryRequest {
    String name;
    String description;
    String imageUrl;
}
