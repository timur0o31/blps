package org.example.blps.controller;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.example.blps.dto.requestDto.UserRequestDto;
import org.example.blps.dto.responseDto.ResponsePaginationDto;
import org.example.blps.entity.Admin;
import org.example.blps.security.CustomUserDetails;
import org.example.blps.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Controller
@RequestMapping("/admins")
@Validated
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PatchMapping("{id}/change-state")
    @PreAuthorize("hasAuthority('CHANGE_STATE')")
    public ResponseEntity<?> changeState(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable @Positive Long id, @RequestParam boolean state) {
        String email = userDetails.getUsername();
        adminService.changeState(email, id, state);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_ADMIN')")
    public ResponseEntity<?> createAdmin(@RequestBody @Valid UserRequestDto userRequestDto) throws IOException {
        adminService.createAdmin(userRequestDto);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_ADMINS')")
    public ResponseEntity<?> getAll(@RequestParam(defaultValue = "0") String page,
                                    @RequestParam(defaultValue = "10") String size) {
        ResponsePaginationDto<Admin> response = adminService.getAll(page, size);
        return ResponseEntity.ok(response);
    }
}
