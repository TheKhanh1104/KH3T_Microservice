package fit.iuh.kh3tshopbe.size.service;

import fit.iuh.kh3tshopbe.exception.AppException;
import fit.iuh.kh3tshopbe.exception.ErrorCode;
import fit.iuh.kh3tshopbe.shared.entity.Size;
import fit.iuh.kh3tshopbe.shared.enums.SizeName;
import fit.iuh.kh3tshopbe.size.repository.SizeRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * [MODULE: size / LAYERED — Business Layer]
 */
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class SizeService {
    SizeRepository sizeRepository;

    public List<Size> getAllSizes() {
        return sizeRepository.findAll();
    }

    public Size getSizeByName(String sizeName) {
        SizeName nameSize = SizeName.valueOf(sizeName);
        return sizeRepository.findByNameSize(nameSize)
                .orElseThrow(() -> new AppException(ErrorCode.SIZE_NOT_FOUND));
    }

    public Size getSizeById(int id) {
        return sizeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SIZE_NOT_FOUND));
    }
}
