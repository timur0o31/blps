package org.example.blps.controller;

import org.apache.coyote.Response;
import org.example.blps.dto.responseDto.CourierApplicationsResponseDto;
import org.example.blps.dto.responseDto.ResponsePaginationDto;
import org.example.blps.entity.CourierRequest;
import org.example.blps.security.CustomUserDetails;
import org.example.blps.service.CourierRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/courier-requests")
public class CourierRequestController {

    private final CourierRequestService courierRequestService;

    @Autowired
    public CourierRequestController(CourierRequestService courierRequestService){
        this.courierRequestService =courierRequestService;
    }
    @PreAuthorize("@accessSecurity.isApprovedAdmin(authentication)")
    @GetMapping
    public ResponseEntity<?> getRequests(@RequestParam(defaultValue = "0") String page, @RequestParam(defaultValue="10") String size){
        ResponsePaginationDto<CourierApplicationsResponseDto> response = courierRequestService.getAll(page,size);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('COURIER')")
    @PostMapping("/submit")
    public ResponseEntity<?> submitRequest(@AuthenticationPrincipal CustomUserDetails userDetails){
        String email = userDetails.getUsername();
        courierRequestService.submitRequest(email);
        return ResponseEntity.ok().build();
    }
    @PreAuthorize("@accessSecurity.isApprovedAdmin(authentication)")
    @PatchMapping("/{id}/approve")
    public ResponseEntity<?> approveRequest(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id){
        String email = userDetails.getUsername();
        courierRequestService.approveRequest(email, id);
        return ResponseEntity.ok().build();
    }
    @PreAuthorize("@accessSecurity.isApprovedAdmin(authentication)")
    @PatchMapping("/{id}/decline")
    public ResponseEntity<?> declineRequest(@AuthenticationPrincipal CustomUserDetails userDetails,@PathVariable Long id){
        String email = userDetails.getUsername();
        courierRequestService.declineRequest(email,id);
        return ResponseEntity.ok().build();
    }
}
