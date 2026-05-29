package fit.iuh.kh3tshopbe.shared.mapper;

import fit.iuh.kh3tshopbe.shared.dto.response.SizeDetailResponse;
import fit.iuh.kh3tshopbe.shared.entity.SizeDetail;
import org.springframework.stereotype.Component;

@Component
public class SizeDetailMapper {

    public SizeDetailResponse toSizeDetailMapper(SizeDetail sizeDetail) {
        if (sizeDetail == null) {
            return null;
        }
        return SizeDetailResponse.builder()
                .id(sizeDetail.getId())
                .quantity(sizeDetail.getQuantity())
                .sizeId(sizeDetail.getSize() != null ? sizeDetail.getSize().getId() : 0)
                .build();
    }
}
