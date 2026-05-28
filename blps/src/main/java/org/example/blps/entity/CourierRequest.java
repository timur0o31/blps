package org.example.blps.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.example.blps.enums.CourierRequestStatus;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name="courier_request")
@Setter
@Getter
public class CourierRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @CreationTimestamp
    @Column(name="creation_date")
    private LocalDateTime creationDate;

    @NotNull
    @Column(name = "request_status", nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    private CourierRequestStatus status = CourierRequestStatus.PENDING;

    @ManyToOne
    @JoinColumn(name="courier_id")
    private Courier courier;

    @ManyToOne
    @JoinColumn(name="reviewed_by_admin_id")
    private Admin reviewedBy;
}
