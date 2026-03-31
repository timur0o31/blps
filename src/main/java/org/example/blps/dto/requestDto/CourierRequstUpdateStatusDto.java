package org.example.blps.dto.requestDto;

import lombok.Getter;
import org.example.blps.enums.CourierStatus;

@Getter
public class CourierRequstUpdateStatusDto {
    private CourierStatus status;
}
