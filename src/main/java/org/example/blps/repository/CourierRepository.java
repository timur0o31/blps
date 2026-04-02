package org.example.blps.repository;

import org.example.blps.entity.Courier;
import org.example.blps.enums.CourierStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourierRepository extends JpaRepository<Courier, Long> {
    public Optional<Courier> findFirstByStatus(CourierStatus status);
    public Optional<Courier> findFirstByStatusAndIdNot(CourierStatus status, Long id);
    Optional<Courier> findByUserId(Long id);
    Optional<Courier> findFirstByStatusAndIdNotIn(CourierStatus status, List<Long> declinedCouriers);
}
