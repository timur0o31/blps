package org.example.blps.repository;

import org.example.blps.entity.Courier;
import org.example.blps.entity.Order;
import org.example.blps.entity.OrderAttempt;
import org.example.blps.enums.OrderAttemptStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderAttemptRepository extends JpaRepository<OrderAttempt, Long> {
    Integer countOrderAttemptByOrderAndStatusIn(Order order, List<OrderAttemptStatus> orderAttemptStatusList);
    Optional<OrderAttempt> findByCourierAndOrderAndStatus(Courier courier, Order order, OrderAttemptStatus assigned);
    List<OrderAttempt> findByOrder(Order order);
    List<OrderAttempt> findTop10ByStatusAndAssigmentAtBefore(
            OrderAttemptStatus status,
            LocalDateTime deadline
    );
}
