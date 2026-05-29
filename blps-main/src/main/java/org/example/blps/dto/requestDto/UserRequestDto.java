package org.example.blps.dto.requestDto;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserRequestDto {
    @NotBlank
    @Size(max = 50)
    @Pattern(regexp = "^[\\p{L}]+$", message = "Имя должно содержать только буквы без пробелов")
    private String name;

    @NotBlank
    @Size(max = 50)
    @Pattern(regexp = "^[\\p{L}]+$", message = "Фамилия должно содержать только буквы без пробелов")
    private String surname;

    @NotBlank
    @Size(max = 255)
//    @Pattern(regexp = "(?i)^[A-Za-z0-9._%+-]+@(yandex\\.ru|gmail\\.com|mail\\.ru)$",
    // **Критерии валидации email**:
    //- Начало: любой символ из набора буквы, цифры, . _ % +
    //- Содержит: символ @
    //- Домен: буквы, цифры, дефис
    //- Заканчивается точкой
    //- Окончание: от 2 до 6 буквенных или цифровых символов
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$",
            message = "Некорректный email! " +
                    "Примеры валидного email: dans2005@yandex.ru, timur2005@gmail.com, sasha2005@mail.ru")
    private String email;

    @NotBlank
    @Size(max = 255)
    @Pattern(regexp = "^\\S+$", message = "Пароль не должен содержать пробелы")
    private String password;

    @NotBlank
    @Size(max = 20)
    @Pattern(regexp = "^(\\+7|8)9\\d{9}$", message ="Номер телефона должен быть в формате +79991234567 или 89991234567")
    private String phoneNumber;
}


