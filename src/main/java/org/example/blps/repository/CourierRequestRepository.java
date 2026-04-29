package org.example.blps.repository;

import org.example.blps.entity.Courier;
import org.example.blps.entity.CourierRequest;
import org.example.blps.enums.CourierRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourierRequestRepository extends JpaRepository<CourierRequest,Long> {
    List<CourierRequest> findCourierRequestByCourierAndStatus(Courier courier, CourierRequestStatus status);
    Optional<CourierRequest> findCourierRequestById(Long id);
    Long countAllByStatus(CourierRequestStatus status);
    List<CourierRequest> findCourierRequestByStatus(CourierRequestStatus status);
}
