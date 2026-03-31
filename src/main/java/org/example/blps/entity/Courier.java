package org.example.blps.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
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

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Column(name="courier_status", nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    private CourierStatus status;

    @OneToMany(mappedBy = "courier")
    private List<Order> orders;

}
