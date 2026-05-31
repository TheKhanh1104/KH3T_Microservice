package fit.iuh.kh3tshopbe.inventory;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    Optional<Inventory> findByProductId(String productId);

    @Modifying
    @Transactional
    @Query("update Inventory i set i.availableQty = i.availableQty - :qty, i.reservedQty = i.reservedQty + :qty where i.productId = :productId and i.availableQty >= :qty")
    int reserve(String productId, int qty);

    @Modifying
    @Transactional
    @Query("update Inventory i set i.reservedQty = i.reservedQty - :qty where i.productId = :productId and i.reservedQty >= :qty")
    int confirm(String productId, int qty);

    @Modifying
    @Transactional
    @Query("update Inventory i set i.availableQty = i.availableQty + :qty, i.reservedQty = i.reservedQty - :qty where i.productId = :productId and i.reservedQty >= :qty")
    int release(String productId, int qty);
}
