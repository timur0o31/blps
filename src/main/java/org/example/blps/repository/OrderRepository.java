package org.example.blps.repository;
import org.example.blps.entity.Courier;
import org.example.blps.entity.Order;
import org.example.blps.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findTop10ByStatus(OrderStatus status);
    Order findByCourierAndStatus(Courier courier, OrderStatus status);
}
