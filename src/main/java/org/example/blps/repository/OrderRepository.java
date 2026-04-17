package org.example.blps.repository;
import org.example.blps.entity.Courier;
import org.example.blps.entity.Order;
import org.example.blps.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findTop10ByStatus(OrderStatus status);
    Order findByCourierAndStatus(Courier courier, OrderStatus status);
    List<Order> findOrderByClientId(Long userId);
    @Query(
            value = """
            SELECT * FROM Orders WHERE client_id = :userId
            ORDER BY id ASC 
            LIMIT :size OFFSET :offset
            """, nativeQuery = true)
    List<Order> findOrdersByClientId(@Param("userId") Long userId, @Param("size") Long size, @Param("offset") Long offset);
}
