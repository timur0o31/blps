package org.example.blps.entity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Setter;

import java.util.List;


@Data
@Setter
@Table(name="clients")
@Entity
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy ="client")
    private List<Order> orders;

}
