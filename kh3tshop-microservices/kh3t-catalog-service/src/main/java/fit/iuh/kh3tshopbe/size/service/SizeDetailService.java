package fit.iuh.kh3tshopbe.size.service;

import fit.iuh.kh3tshopbe.exception.AppException;
import fit.iuh.kh3tshopbe.exception.ErrorCode;
import fit.iuh.kh3tshopbe.product.repository.ProductRepository;
import fit.iuh.kh3tshopbe.shared.dto.request.SizeDetailRequest;
import fit.iuh.kh3tshopbe.shared.dto.response.SizeDetailResponse;
import fit.iuh.kh3tshopbe.shared.entity.Product;
import fit.iuh.kh3tshopbe.shared.entity.Size;
import fit.iuh.kh3tshopbe.shared.entity.SizeDetail;
import fit.iuh.kh3tshopbe.shared.mapper.SizeDetailMapper;
import fit.iuh.kh3tshopbe.size.repository.SizeDetailRepository;
import fit.iuh.kh3tshopbe.size.repository.SizeRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

/**
 * [MODULE: size / LAYERED — Business Layer]
 */
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class SizeDetailService {
    SizeDetailRepository sizeDetailRepository;
    ProductRepository productRepository;
    SizeRepository sizeRepository;
    SizeDetailMapper sizeDetailMapper;

    public SizeDetailResponse findByProductAndSize(SizeDetailRequest sizeDetailRequest) {
        Product product =  productRepository.findById(sizeDetailRequest.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        Size size = sizeRepository.findById(sizeDetailRequest.getSizeId())
                .orElseThrow(() -> new AppException(ErrorCode.SIZE_NOT_FOUND));

        SizeDetail sizeDetail = sizeDetailRepository.findSizeDetailByProductAndSize(product, size);

        return sizeDetailMapper.toSizeDetailMapper(sizeDetail);
    }

    public SizeDetailResponse findById(int sizeId) {
         SizeDetail sizeDetail = sizeDetailRepository.findById(sizeId)
                 .orElseThrow(() -> new AppException(ErrorCode.SIZE_DETAIL_NOT_FOUND));

         return sizeDetailMapper.toSizeDetailMapper(sizeDetail);
    }
}
