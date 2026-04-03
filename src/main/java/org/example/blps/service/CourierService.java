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
    public CourierService(CourierRepository courierRepository, UserService userService) {
        this.courierRepository = courierRepository;
        this.userService = userService;
    }

    public void toggleCourierShiftStatus(String email) {
        User user = userService.findByEmail(email);
        Courier courier = courierRepository.findByUserId(user.getId()).orElseThrow(() -> new RuntimeException("Курьер не найден"));
        if (courier.getStatus()==CourierStatus.END_SHIFT){
            throw new RuntimeException("Разберитесь с назначенным заказом!");
        }
        if (courier.getStatus() == CourierStatus.OFF_SHIFT) {
            courier.setStatus(CourierStatus.ON_SHIFT);
        } else if (courier.getStatus() == CourierStatus.ON_SHIFT) {
            courier.setStatus(CourierStatus.OFF_SHIFT);
        } else if (courier.getStatus() == CourierStatus.BUSY || courier.getStatus() == CourierStatus.ACCEPTING_ORDER){
            courier.setStatus(CourierStatus.END_SHIFT);
        }
        courierRepository.save(courier);
    }

    public Courier findOnlineCourier(List<Long> declinedCouriers){
        if (declinedCouriers.isEmpty()) return courierRepository.findFirstByStatus(CourierStatus.ON_SHIFT).orElse(null);
        return courierRepository.findFirstByStatusAndIdNotIn(CourierStatus.ON_SHIFT, declinedCouriers).orElse(null);
    }

    public Courier findCourierByEmail(String email) {
        return courierRepository.findByUserId(userService.findByEmail(email).getId()).orElseThrow(() -> new RuntimeException("Курьера с таким email не существует!"));
    }

    public Courier findCourierWithOnlineStatus() {
        return courierRepository.findFirstByStatus(CourierStatus.ON_SHIFT).orElse(null);
    }

    public void saveCourier(Courier courier) {
        courierRepository.save(courier);
    }

}

