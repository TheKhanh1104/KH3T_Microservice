package fit.iuh.kh3tshopbe.repository;

import fit.iuh.kh3tshopbe.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Integer> {
    Optional<Category> findByName(String name);
}
