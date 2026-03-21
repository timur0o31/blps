package org.example.blps.dto.requestDto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.blps.status.OrderStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusRequestDto {
    private OrderStatus orderStatus;
}
