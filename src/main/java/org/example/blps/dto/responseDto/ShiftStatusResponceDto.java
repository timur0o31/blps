package org.example.blps.dto.responseDto;
import lombok.Getter;
import lombok.Setter;
import org.example.blps.enums.CourierStatus;

@Getter
@Setter
public class ShiftStatusResponceDto {
    private CourierStatus courierStatus;

    public ShiftStatusResponceDto(CourierStatus courierStatus) {
        this.courierStatus = courierStatus;
    }
}
