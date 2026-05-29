package fit.iuh.kh3tshopbe.size.repository;

import fit.iuh.kh3tshopbe.shared.entity.Product;
import fit.iuh.kh3tshopbe.shared.entity.Size;
import fit.iuh.kh3tshopbe.shared.entity.SizeDetail;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * [MODULE: size] — Repository cho SizeDetail entity.
 */
public interface SizeDetailRepository extends JpaRepository<SizeDetail, Integer> {
    SizeDetail findSizeDetailByProductAndSize(Product product, Size size);
}
