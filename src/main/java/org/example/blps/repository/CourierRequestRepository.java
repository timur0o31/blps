package org.example.blps.repository;

import org.example.blps.entity.Courier;
import org.example.blps.entity.CourierRequest;
import org.example.blps.enums.CourierRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourierRequestRepository extends JpaRepository<CourierRequest,Long> {
    List<CourierRequest> findCourierRequestByCourierAndStatus(Courier courier, CourierRequestStatus status);
    Optional<CourierRequest> findCourierRequestById(Long id);
    Long countAllByStatus(CourierRequestStatus status);
    @Query("""
            select cr from CourierRequest cr
            where (:status is null or cr.status = :status)
            and (:courierId is null or cr.courier.id = :courierId)
            """)
    Page<CourierRequest> findWithfilters(Pageable pageable,@Param("status") CourierRequestStatus status,
                                         @Param("courierId") Long courierId);
}
