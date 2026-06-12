package org.example.blps.dto.requestDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.example.blps.enums.OrderStatus;

@Getter
@Setter
public class BitrixRequestDto {
    @NotBlank
    private Long backendId;

    @NotBlank
    @Size(max = 150)
    private OrderStatus status;
}