package org.example.blps.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.example.blps.enums.CourierAccountState;
import org.example.blps.enums.CourierStatus;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "couriers")
public class Courier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, name = "user_id")
    private Long userId;

    @NotNull
    @Column(name="courier_status", nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    private CourierStatus status;

    @NotNull
    @Column(name = "courier_account_state", nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    private CourierAccountState accountState;

    @OneToMany(mappedBy = "courier")
    private List<Order> orders;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deleted_by_admin_id")
    private Admin deletedBy;

    @OneToMany(mappedBy="courier")
    private List<CourierRequest> courierRequests;
}
