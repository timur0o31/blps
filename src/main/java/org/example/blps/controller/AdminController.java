package org.example.blps.controller;

import jakarta.validation.Valid;
import org.example.blps.dto.requestDto.UserRequestDto;
import org.example.blps.security.CustomUserDetails;
import org.example.blps.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Controller
@RequestMapping("/admins")
public class AdminController {
    private final AdminService adminService;
    public AdminController(AdminService adminService){
        this.adminService=adminService;
    }

    //подправить
    @PatchMapping("{id}/change-state")
    @PreAuthorize("@accessSecurity.isApprovedAdmin(authentication) or @accessSecurity.isSuperUser(authentication)")
    public ResponseEntity<?> changeState(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id, @RequestParam boolean state){
        String email = userDetails.getUsername();
        adminService.changeState(email,id,state);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    @PreAuthorize("@accessSecurity.isApprovedAdmin(authentication) or @accessSecurity.isSuperUser(authentication)")
    public ResponseEntity<?> createAdmin(@RequestBody @Valid UserRequestDto userRequestDto) throws IOException {
        adminService.createAdmin(userRequestDto);
        return ResponseEntity.ok().build();
    }
}
