package fit.iuh.kh3tshopbe.shared.dto.request;

import fit.iuh.kh3tshopbe.shared.enums.SizeName;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SizeRequest {
    SizeName nameSize;
}
