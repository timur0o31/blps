package org.example.blps.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.example.blps.status.CourierStatus;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "couriers")
public class Courier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @NotNull
    @Column(name = "surname", nullable = false, length = 50)
    private String surname;

    @NotNull
    @Column(name="courier_status", nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    private CourierStatus status;

    @OneToMany(mappedBy = "courier")
    private List<Order> orders;

}
