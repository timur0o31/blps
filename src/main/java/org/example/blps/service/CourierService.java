package org.example.blps.service;
import org.example.blps.dto.requestDto.CourierRequstUpdateStatusDto;
import org.example.blps.entity.Courier;
import org.example.blps.entity.User;
import org.example.blps.enums.CourierStatus;
import org.example.blps.repository.CourierRepository;
import org.example.blps.security.CustomUserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourierService {

    private final CourierRepository courierRepository;
    private final UserService userService;

    @Autowired
    public CourierService(CourierRepository courierRepository, UserService userService ) {
        this.courierRepository = courierRepository;
        this.userService = userService;
    }

    public Courier updateCourierStatus(String email, CourierRequstUpdateStatusDto courierRequstUpdateStatusDto) {
        User user = userService.findByEmail(email);
        Courier courier = courierRepository.findByUserId(user.getId()).orElseThrow(() -> new RuntimeException("Курьер не найден"));
        courier.setStatus(courierRequstUpdateStatusDto.getStatus());
        return courierRepository.save(courier);
    }

    public Courier findOnlineCourier(List<Long> declinedCouriers){
        if (declinedCouriers.isEmpty()) return courierRepository.findFirstByStatus(CourierStatus.ONLINE).orElse(null);
        return courierRepository.findFirstByStatusAndIdNotIn(CourierStatus.ONLINE, declinedCouriers).orElse(null);
    }

    public Courier findCourierByEmail(String email) {
        return courierRepository.findByUserId(userService.findByEmail(email).getId()).orElseThrow(() -> new RuntimeException("Курьера с таким email не существует!"));
    }

    public Courier findCourierWithOnlineStatus() {
        return courierRepository.findFirstByStatus(CourierStatus.ONLINE).orElse(null);
    }

    public void saveCourier(Courier courier) {
        courierRepository.save(courier);
    }

}

