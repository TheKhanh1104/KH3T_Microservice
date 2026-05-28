package fit.iuh.kh3tshopbe.dto.request;

import lombok.Data;

@Data
public class RefreshRequest {
    private String username;
    private String refreshToken;
}
