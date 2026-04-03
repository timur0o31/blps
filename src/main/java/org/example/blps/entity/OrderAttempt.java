package org.example.blps.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.blps.enums.OrderAttemptStatus;

import java.time.LocalDateTime;
@Entity
@Table(name="order_attempts")
@Getter
@Setter
public class OrderAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="courier_id")
    private Courier courier;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="order_id")
    private Order order;
    @Column(name="assigment_at")
    private LocalDateTime assigmentAt;
    @NotNull
    @Column(name = "order_attempt_status", nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    private OrderAttemptStatus status;

}
