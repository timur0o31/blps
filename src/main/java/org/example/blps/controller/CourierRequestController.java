package org.example.blps.controller;


import jakarta.validation.constraints.Positive;
import org.example.blps.dto.responseDto.CourierApplicationsResponseDto;
import org.example.blps.dto.responseDto.ResponsePaginationDto;
import org.example.blps.security.CustomUserDetails;
import org.example.blps.service.CourierRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Controller
@Validated
@RequestMapping("/courier-requests")
public class CourierRequestController {

    private final CourierRequestService courierRequestService;

    @Autowired
    public CourierRequestController(CourierRequestService courierRequestService){
        this.courierRequestService =courierRequestService;
    }

    @PreAuthorize("hasAuthority('VIEW_COURIER_APPLICATIONS')")
    @GetMapping
    public ResponseEntity<?> getRequests(@RequestParam(defaultValue = "0") String page, @RequestParam(defaultValue="10") String size,
                                         @RequestParam(required = false) @Positive Long courierId, @RequestParam(required = false) String status){
        ResponsePaginationDto<CourierApplicationsResponseDto> response = courierRequestService.getAll(page,size, courierId, status);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAuthority('SUMBMIT_REQUEST')")
    @PostMapping("/submit")
    public ResponseEntity<?> submitRequest(@AuthenticationPrincipal CustomUserDetails userDetails){
        String email = userDetails.getUsername();
        courierRequestService.submitRequest(email);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAuthority('APPROVE_REQUEST')")
    @PatchMapping("/{id}/approve")
    public ResponseEntity<?> approveRequest(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable @Positive Long id){
        String email = userDetails.getUsername();
        courierRequestService.approveRequest(email, id);
        return ResponseEntity.ok().build();
    }
    @PreAuthorize("hasAuthority('DECLINE_REQUEST')")
    @PatchMapping("/{id}/decline")
    public ResponseEntity<?> declineRequest(@AuthenticationPrincipal CustomUserDetails userDetails,@PathVariable @Positive Long id){
        String email = userDetails.getUsername();
        courierRequestService.declineRequest(email,id);
        return ResponseEntity.ok().build();
    }
}
