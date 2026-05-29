package fit.iuh.kh3tshopbe.size.repository;

import fit.iuh.kh3tshopbe.shared.entity.Size;
import fit.iuh.kh3tshopbe.shared.enums.SizeName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * [MODULE: size] — Repository cho Size entity.
 */
public interface SizeRepository extends JpaRepository<Size, Integer> {
    Optional<Size> findByNameSize(SizeName nameSize);
}
