package fit.iuh.kh3tshopbe.category.repository;

import fit.iuh.kh3tshopbe.shared.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * [MODULE: category] — Repository cho Category entity.
 */
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    Optional<Category> findByName(String name);
}
