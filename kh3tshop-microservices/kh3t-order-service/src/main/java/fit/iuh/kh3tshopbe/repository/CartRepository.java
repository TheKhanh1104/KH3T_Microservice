package fit.iuh.kh3tshopbe.repository;

import fit.iuh.kh3tshopbe.entities.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Integer> {
    Cart findByAccountId(int accountId);
}
