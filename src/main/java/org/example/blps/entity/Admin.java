package org.example.blps.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, name = "user_id")
    private Long userId;

    @OneToMany(mappedBy = "approvedBy")
    private List<Courier> approvedCouriers;

    @OneToMany(mappedBy = "deletedBy")
    private List<Courier> deletedCouriers;
}
