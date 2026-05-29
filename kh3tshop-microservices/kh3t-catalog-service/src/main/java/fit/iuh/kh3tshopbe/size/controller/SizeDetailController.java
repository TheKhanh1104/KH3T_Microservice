package fit.iuh.kh3tshopbe.size.controller;

import fit.iuh.kh3tshopbe.shared.dto.request.SizeDetailRequest;
import fit.iuh.kh3tshopbe.shared.dto.response.SizeDetailResponse;
import fit.iuh.kh3tshopbe.size.service.SizeDetailService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

/**
 * [MODULE: size / LAYERED — Web Layer]
 */
@RestController
@RequestMapping("/size-details")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SizeDetailController {
    SizeDetailService sizeDetailService;

    @GetMapping("/find")
    public SizeDetailResponse getSizeDetailByProductAndSize(@RequestParam("productId") int productId,
                                                            @RequestParam("sizeId") int sizeId) {
        SizeDetailRequest request = SizeDetailRequest.builder()
                .productId(productId)
                .sizeId(sizeId)
                .build();
        return sizeDetailService.findByProductAndSize(request);
    }
}
