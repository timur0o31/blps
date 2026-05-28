package org.example.blps.service.consumers.Repository;

import org.example.blps.entity.Order;
import org.example.blps.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderConsumerRepository extends JpaRepository<Order, Long> {
    List<Order> findTop10ByStatus(OrderStatus status);

}
