package org.example.blps.service;
import org.example.blps.dto.requestDto.CourierRequstUpdateStatusDto;
import org.example.blps.entity.Courier;
import org.example.blps.entity.User;
import org.example.blps.repository.CourierRepository;
import org.example.blps.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CourierService {

    private final CourierRepository courierRepository;
    private final UserService userService;

    @Autowired
    public CourierService(CourierRepository courierRepository, UserService userService ) {
        this.courierRepository = courierRepository;
        this.userService = userService;
    }

    public Courier updateCourierStatus(CourierRequstUpdateStatusDto courierRequstUpdateStatusDto) {
        CustomUserDetails customUserDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email = customUserDetails.getUsername();
        User user = userService.findByEmail(email);
        Courier courier = courierRepository.findByUserId(user.getId()).orElseThrow(() -> new RuntimeException("Courier not found"));
        courier.setStatus(courierRequstUpdateStatusDto.getStatus());
        return courierRepository.save(courier);
    }
}

