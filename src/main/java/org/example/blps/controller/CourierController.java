package org.example.blps.controller;
import jakarta.validation.constraints.Positive;
import org.example.blps.dto.responseDto.CourierResponseDto;
import org.example.blps.dto.responseDto.ResponsePaginationDto;
import org.example.blps.dto.responseDto.ShiftStatusResponceDto;
import org.example.blps.enums.CourierStatus;
import org.example.blps.security.CustomUserDetails;
import org.example.blps.service.CourierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/couriers")
@Validated
public class CourierController {

    private final CourierService courierService;

    @Autowired
    public CourierController(CourierService courierService) {
        this.courierService = courierService;
    }

    @PreAuthorize("@accessSecurity.isApprovedCourier(authentication)")
    @PatchMapping("/shift-status")
    public ResponseEntity<ShiftStatusResponceDto> toggleShiftStatus(@AuthenticationPrincipal CustomUserDetails userDetails) {
        String email = userDetails.getUsername();
        CourierStatus status = courierService.toggleCourierShiftStatus(email);
        return ResponseEntity.ok(new ShiftStatusResponceDto(status));
    }
    @PreAuthorize("@accessSecurity.isApprovedAdmin(authentication)")
    @PatchMapping("/{id}/block")
    public ResponseEntity<?> blockCourier(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable @Positive Long id){
        String email = userDetails.getUsername();
        courierService.blockCourier(email,id);
        return ResponseEntity.ok("");
    }
    @PreAuthorize("@accessSecurity.isApprovedAdmin(authentication)")
    @GetMapping
    public ResponseEntity<?> getAll(@RequestParam(defaultValue = "0") String page, @RequestParam(defaultValue="10") String size,
                                    @RequestParam(required = false) String courierState, @RequestParam(required = false) String courierStatus) {
        ResponsePaginationDto<CourierResponseDto> response = courierService.getAll(page,size, courierState, courierStatus);
        return ResponseEntity.ok(response);
    }
}

