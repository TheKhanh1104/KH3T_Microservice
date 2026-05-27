// OrderDetailRepository.java
package fit.iuh.kh3tshopbe.repository;
import fit.iuh.kh3tshopbe.entities.OrderDetail;


import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;



import java.util.Date;


import java.util.List;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {

    @Query("SELECT od.productId, SUM(od.quantity) " +
            "FROM OrderDetail od " +
            "WHERE od.productId IS NOT NULL " +
            "GROUP BY od.productId")
    List<Object[]> findSoldQuantityByProductId();

    // Hoặc nếu muốn map trực tiếp
    @Query("SELECT od.productId, COALESCE(SUM(od.quantity), 0) " +
            "FROM OrderDetail od " +
            "WHERE od.productId = :productId " +
            "GROUP BY od.productId")
    Long findSoldQuantityByProductId(@Param("productId") Integer productId);

    @Query("""
        SELECT od.productId, SUM(od.quantity), SUM(od.totalPrice)
        FROM OrderDetail od
        WHERE od.order.orderDate BETWEEN :start AND :end
        GROUP BY od.productId
        ORDER BY SUM(od.quantity) DESC
        limit 10
        """)
    List<Object[]> getTopTrending(Date start, Date end, Pageable pageable);


    // Để tính trend, phải lấy dữ liệu kỳ trước
    @Query("""
        SELECT od.productId, SUM(od.quantity)
        FROM OrderDetail od
        WHERE od.order.orderDate BETWEEN :start AND :end
        GROUP BY od.productId
        """)
    List<Object[]> getSalesInPeriod(Date start, Date end, Pageable pageable);


}