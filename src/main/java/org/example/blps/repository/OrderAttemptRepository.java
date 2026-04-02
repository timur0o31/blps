package org.example.blps.repository;

import org.example.blps.entity.Courier;
import org.example.blps.entity.Order;
import org.example.blps.entity.OrderAttempt;
import org.example.blps.enums.OrderAttemptStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderAttemptRepository extends JpaRepository<OrderAttempt, Long> {
    public Integer countOrderAttemptByOrderAndStatusIn(Order order, List<OrderAttemptStatus> orderAttemptStatusList);
    public OrderAttempt findByCourierAndOrder(Courier courier,Order order);
    public List<Long> findCourierIdsByOrOrder(Order order);
    public List<OrderAttempt> findTop10ByStatusAndAssigmentAtBefore(
            OrderAttemptStatus status,
            LocalDateTime deadline
    );
}
