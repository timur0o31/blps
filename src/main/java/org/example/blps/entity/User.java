package org.example.blps.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name="users")
public class User {
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
    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @NotNull
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @NotNull
    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

}
