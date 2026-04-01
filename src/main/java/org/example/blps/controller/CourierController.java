package org.example.blps.controller;
import jakarta.annotation.security.RolesAllowed;
import org.example.blps.dto.requestDto.CourierRequstUpdateStatusDto;
import org.example.blps.entity.Courier;
import org.example.blps.security.CustomUserDetails;
import org.example.blps.service.CourierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    @Secured("ROLE_COURIER")
    public Courier updateStatus(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody CourierRequstUpdateStatusDto courierRequstUpdateStatusDto) {
        String email = userDetails.getUsername();
        return courierService.updateCourierStatus(email,courierRequstUpdateStatusDto);
    }
}

