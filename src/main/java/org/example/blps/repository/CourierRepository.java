package org.example.blps.repository;

import org.example.blps.entity.Courier;
import org.example.blps.entity.CourierRequest;
import org.example.blps.enums.CourierAccountState;
import org.example.blps.enums.CourierRequestStatus;
import org.example.blps.enums.CourierStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CourierRepository extends JpaRepository<Courier, Long> {
    Optional<Courier> findByUserId(Long id);
    @Query("""
            select c from Courier c
            where (:status is null or c.status = :status)
            and (:accountState is null or c.accountState = :accountState)
            """)
    Page<Courier> findWithfilters(Pageable pageable, @Param("status") CourierStatus status,
                                         @Param("accountState") CourierAccountState accountState);
}
