package org.example.blps.controller;
import org.example.blps.security.CustomUserDetails;
import org.example.blps.service.CourierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PreAuthorize("hasRole('COURIER')")
    @PostMapping("/shift-status")
    public void toggleShiftStatus(@AuthenticationPrincipal CustomUserDetails userDetails) {
        String email = userDetails.getUsername();
        courierService.toggleCourierShiftStatus(email);
    }
}

