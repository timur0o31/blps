package org.example.blps.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.example.blps.enums.OrderStatus;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name="orders")
@Getter
@Setter
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name= "content", nullable = false, length = 150)
    private String content;

    @NotNull
    @Column(name= "address", nullable = false, length = 150)
    private String address;

    @NotNull
    @Column(name = "order_status", nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.NEW;

    @CreationTimestamp
    @Column(name="creation_date")
    private LocalDateTime creationDate;

    @Column(name="waiting_cycles")
    private Integer waitingCycles=0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="courier_id")
    private Courier courier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="client_id")
    private Client client;

    @OneToMany(mappedBy = "order")
    private List<OrderAttempt> orderAttempts;
}
