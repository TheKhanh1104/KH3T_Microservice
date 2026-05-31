package fit.iuh.kh3tshopbe.shared.dto.response;

import fit.iuh.kh3tshopbe.shared.enums.Status;
import lombok.*;

import java.util.Date;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductResponse {
    private int id;
    private String name;
    private String description;
    private double price;
    private double costPrice;
    private String unit;
    private int quantity;
    private String imageUrlFront;
    private String imageUrlBack;
    private Date createdAt;
    private Date updatedAt;
    private double rating;
    private CategoryResponse category;
    private double discountAmount;
    private String material;
    private String form;
    private Long soldQuantity;
    private Status status;
    private List<SizeDetailResponse> sizeDetails;

    @Data @Builder
    public static class SizeDetailResponse {
        private int id;
        private String sizeName;
        private int quantity;
    }
}
