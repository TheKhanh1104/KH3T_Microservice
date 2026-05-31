package fit.iuh.kh3tshopbe.inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReservationRecordRepository extends JpaRepository<ReservationRecord, UUID> {
}
