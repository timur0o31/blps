package org.example.blps.service.consumers.Repository;

import org.example.blps.entity.Courier;
import org.example.blps.enums.CourierStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourierConsumer extends JpaRepository<Courier, Long> {
    Optional<Courier> findFirstByStatus(CourierStatus status);
    Optional<Courier> findFirstByStatusAndIdNotIn(CourierStatus status, List<Long> declinedCouriers);

}
