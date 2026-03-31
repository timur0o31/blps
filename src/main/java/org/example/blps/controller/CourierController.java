package org.example.blps.controller;
import org.example.blps.dto.requestDto.CourierRequstUpdateStatusDto;
import org.example.blps.entity.Courier;
import org.example.blps.service.CourierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/courier")
public class CourierController {

    private final CourierService courierService;

    @Autowired
    public CourierController(CourierService courierService) {
        this.courierService = courierService;
    }

    @PostMapping("/status")
    public Courier updateStatus(@RequestBody CourierRequstUpdateStatusDto courierRequstUpdateStatusDto) {
        return courierService.updateCourierStatus(courierRequstUpdateStatusDto);
    }
}

