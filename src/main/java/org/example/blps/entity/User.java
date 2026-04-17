package org.example.blps.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.example.blps.enums.Role;

@Entity
@Getter
@Setter
@Table(name="users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @NotBlank
    @Size(max = 50)
    @Column(name = "surname", nullable = false, length = 50)
    private String surname;

    @NotBlank
    @Email
    // (?i) - не учитывать регистр
    // ^ - проверка начинается с первого символа
    // [A-Za-z0-9._%+-] - разрешенные символы
    // @ - должен быть символ @
    // (yandex\\.ru|gmail\\.com|mail\\.ru) - доступные домены
    // $ - конец строки
    @Pattern(regexp = "(?i)^[A-Za-z0-9._%+-]+@(yandex\\\\.ru|gmail\\\\.com|mail\\\\.ru)$")
    @Size(max = 255)
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @NotBlank
    @Size(max = 255)
    @Column(name = "password", nullable = false)
    private String password;

    @NotBlank
    @Size(max = 20)

    @Pattern(regexp = "^(\\+7|8)9\\d{9}$")
    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @NotNull
    @Column(name="user_role", nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    private Role role;
}
