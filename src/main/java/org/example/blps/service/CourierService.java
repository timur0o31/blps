package org.example.blps.service;
import jakarta.persistence.EntityNotFoundException;
import org.example.blps.entity.Admin;
import org.example.blps.entity.Courier;
import org.example.blps.entity.User;
import org.example.blps.enums.CourierAccountState;
import org.example.blps.enums.CourierStatus;
import org.example.blps.repository.CourierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourierService {

    private final CourierRepository courierRepository;
    private final UserService userService;
    private final AdminService adminService;
    @Autowired
    public CourierService(CourierRepository courierRepository, UserService userService, AdminService adminService) {
        this.courierRepository = courierRepository;
        this.userService = userService;
        this.adminService = adminService;
    }

    public CourierStatus toggleCourierShiftStatus(String email) {
        User user = userService.findByEmail(email);
        Courier courier = courierRepository.findByUserId(user.getId()).orElseThrow(() -> new EntityNotFoundException("Курьер не найден"));
        if (courier.getStatus()==CourierStatus.END_SHIFT){
            throw new IllegalStateException("Разберитесь с назначенным заказом!");
        }
        if (courier.getStatus() == CourierStatus.OFF_SHIFT) {
            courier.setStatus(CourierStatus.ON_SHIFT);
        } else if (courier.getStatus() == CourierStatus.ON_SHIFT) {
            courier.setStatus(CourierStatus.OFF_SHIFT);
        } else if (courier.getStatus() == CourierStatus.BUSY || courier.getStatus() == CourierStatus.ACCEPTING_ORDER){
            courier.setStatus(CourierStatus.END_SHIFT);
        }
        courierRepository.save(courier);
        return courier.getStatus();
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

    public Courier blockCourier(String email, Long id){
        Admin admin = adminService.findByUserId(userService.findByEmail(email).getId());
        Courier courier = courierRepository.findById(id).orElseThrow(
                ()->new RuntimeException("Курьера с данным id не существует"));
        if (courier.getAccountState()==CourierAccountState.BLOCKED){
            throw new RuntimeException("Курьер уже был заблокирован");
        }
        if (courier.getStatus() == CourierStatus.BUSY
                || courier.getStatus() == CourierStatus.ACCEPTING_ORDER
                || courier.getStatus() == CourierStatus.END_SHIFT) {
            throw new IllegalStateException("Нельзя заблокировать курьера во время выполнения заказа");
        }
        courier.setAccountState(CourierAccountState.BLOCKED);
        courier.setStatus(CourierStatus.OFF_SHIFT);
        courier.setDeletedBy(admin);
        return courierRepository.save(courier);
    }
}

