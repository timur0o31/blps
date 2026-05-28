package org.example.blps.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name="admins")
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, name = "user_id")
    private Long userId;

    @NotNull
    @Column(name = "account_state", nullable = false)
    private boolean accountState;

    @OneToMany(mappedBy = "deletedBy")
    private List<Courier> deletedCouriers;

    @OneToMany(mappedBy="reviewedBy")
    private List<CourierRequest> courierRequests;

}
