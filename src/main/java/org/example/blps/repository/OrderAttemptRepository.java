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
    public Integer countOrderAttemptByOrderAndStatusIn(Order order, List<OrderAttemptStatus> orderAttemptStatusList);
    public Optional<OrderAttempt> findByCourierAndOrderAndStatus(Courier courier, Order order, OrderAttemptStatus assigned);
    public List<OrderAttempt> findByOrder(Order order);
    public List<OrderAttempt> findTop10ByStatusAndAssigmentAtBefore(
            OrderAttemptStatus status,
            LocalDateTime deadline
    );
}
