package fit.iuh.kh3tshopbe.mapper;

import fit.iuh.kh3tshopbe.dto.response.SizeDetailResponse;
import fit.iuh.kh3tshopbe.entities.SizeDetail;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.11 (Oracle Corporation)"
)
@Component
public class SizeDetailMapperImpl implements SizeDetailMapper {

    @Override
    public SizeDetailResponse toSizeDetailMapper(SizeDetail sizeDetail) {
        if ( sizeDetail == null ) {
            return null;
        }

        SizeDetailResponse.SizeDetailResponseBuilder sizeDetailResponse = SizeDetailResponse.builder();

        sizeDetailResponse.id( sizeDetail.getId() );
        sizeDetailResponse.quantity( sizeDetail.getQuantity() );

        return sizeDetailResponse.build();
    }
}
