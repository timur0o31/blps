package org.example.blps.service;
import org.example.blps.dto.requestDto.CourierRequstUpdateStatusDto;
import org.example.blps.entity.Courier;
import org.example.blps.entity.User;
import org.example.blps.repository.CourierRepository;
import org.example.blps.security.CustomUserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CourierService {

    private final CourierRepository courierRepository;
    private final UserService userService;
    private final CustomUserServiceImpl customUserService;

    @Autowired
    public CourierService(CourierRepository courierRepository, UserService userService, CustomUserServiceImpl customUserService ) {
        this.courierRepository = courierRepository;
        this.userService = userService;
        this.customUserService = customUserService;

    }

    public Courier updateCourierStatus(String email, CourierRequstUpdateStatusDto courierRequstUpdateStatusDto) {
        User user = userService.findByEmail(email);
        Courier courier = courierRepository.findByUserId(user.getId()).orElseThrow(() -> new RuntimeException("Courier not found"));
        courier.setStatus(courierRequstUpdateStatusDto.getStatus());
        return courierRepository.save(courier);
    }
}

