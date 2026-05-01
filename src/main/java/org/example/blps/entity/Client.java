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

    @Column(unique = true, nullable = false, name = "user_id")
    private Long userId;

    @OneToMany(mappedBy ="client")
    private List<Order> orders;

}
