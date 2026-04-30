package org.example.blps.service;
import jakarta.persistence.EntityNotFoundException;
import org.example.blps.dto.responseDto.CourierApplicationsResponseDto;
import org.example.blps.dto.responseDto.CourierResponseDto;
import org.example.blps.dto.responseDto.ResponsePaginationDto;
import org.example.blps.entity.Admin;
import org.example.blps.entity.Courier;
import org.example.blps.entity.CourierRequest;
import org.example.blps.entity.User;
import org.example.blps.enums.CourierAccountState;
import org.example.blps.enums.CourierRequestStatus;
import org.example.blps.enums.CourierStatus;
import org.example.blps.mapper.CourierMapper;
import org.example.blps.mapper.CourierRequestMapper;
import org.example.blps.repository.CourierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CourierService {

    private final CourierRepository courierRepository;
    private final UserService userService;
    private final AdminService adminService;
    private final CourierMapper mapper;
    @Autowired
    public CourierService(CourierRepository courierRepository, UserService userService, AdminService adminService,
                          CourierMapper mapper) {
        this.courierRepository = courierRepository;
        this.userService = userService;
        this.adminService = adminService;
        this.mapper = mapper;
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
    public ResponsePaginationDto<CourierResponseDto> getAll(String page, String size){
        Long pageValue;
        Long sizeValue;
        try {
            pageValue = Long.parseLong(page);
            sizeValue = Long.parseLong(size);
            if (pageValue < 0) throw new IllegalArgumentException("page должен быть не отрицательным целым числом");
            if (sizeValue <= 0) throw new IllegalArgumentException("size должен быть положитеным целым числом!");
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Параметры page и size должны быть целыми числами");
        }
        List<CourierResponseDto> result = new ArrayList<>();
        List<Courier> couriers= courierRepository.findAll();
        Long totalElements = courierRepository.count();
        Long totalPages = 0L;
        if (totalElements/sizeValue!=0) totalPages = totalElements/sizeValue+1;
        Long lastPage = 0L;
        if (totalPages!=0) lastPage = totalPages-1;
        for (Courier cr: couriers){
            result.add(mapper.fromEntityToDto(cr));
        }
        return new ResponsePaginationDto(result, page, size, totalElements,lastPage,0L,totalPages);
    }
}

