package org.example.blps.repository;
import org.example.blps.entity.Courier;
import org.example.blps.entity.Order;
import org.example.blps.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Order findByCourierAndStatus(Courier courier, OrderStatus status);
    Page<Order> findOrdersByClientId(Long clientId, Pageable pageable);
    Long countOrderByClientId(Long userId);
    List<Order> findTop10ByStatus(OrderStatus status);
}
