package org.example.blps.repository;

import org.example.blps.entity.Order;
import org.example.blps.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    public List<Order> findTop10ByStatusAndAssigmentAtBefore(OrderStatus status, LocalDateTime now);
    public List<Order> findTop10ByStatus(OrderStatus status);
}
