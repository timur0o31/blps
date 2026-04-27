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
    @Pattern(regexp = "^[\\p{L}]+$", message = "Имя должно содержать только буквы без пробелов")
    private String name;

    @NotBlank
    @Size(max = 50)
    @Column(name = "surname", nullable = false, length = 50)
    @Pattern(regexp = "^[\\p{L}]+$", message = "Фамилия должно содержать только буквы без пробелов")
    private String surname;

    @NotBlank
    // (?i) - не учитывать регистр
    // ^ - проверка начинается с первого символа
    // [A-Za-z0-9._%+-] - разрешенные символы
    // @ - должен быть символ @
    // (yandex\\.ru|gmail\\.com|mail\\.ru) - доступные домены
    // $ - конец строки
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$",
            message = "Некорректный email! " +
                    "Примеры валидного email: dans2005@yandex.ru, timur2005@gmail.com, sasha2005@mail.ru")
    @Size(max = 255)
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @NotBlank
    @Size(max = 255)
    @Column(name = "password", nullable = false)
    @Pattern(regexp = "^\\S+$", message = "Пароль не должен содержать пробелы")
    private String password;

    @NotBlank
    @Size(max = 12)
    @Pattern(regexp = "^(\\+7|8)9\\d{9}$", message ="Номер телефона должен быть в формате +79991234567 или 89991234567")
    @Column(name = "phone_number", nullable = false, length = 12)
    private String phoneNumber;

    @NotNull
    @Column(name="user_role", nullable = false, length = 15)
    @Enumerated(EnumType.STRING)
    private Role role;
}
