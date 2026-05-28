package fit.iuh.kh3tshopbe.saga.repository;

import fit.iuh.kh3tshopbe.saga.domain.OrderStatus;
import fit.iuh.kh3tshopbe.saga.domain.SagaOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SagaOrderRepository extends JpaRepository<SagaOrder, UUID> {
    List<SagaOrder> findAllByOrderByCreatedAtDesc();

    List<SagaOrder> findByStatus(OrderStatus status);
}