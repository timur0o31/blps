package org.example.blps.service;
import jakarta.persistence.EntityNotFoundException;
import org.example.blps.annotations.isApprovedAdmin;
import org.example.blps.annotations.isApprovedCourier;
import org.example.blps.dto.responseDto.CourierApplicationsResponseDto;
import org.example.blps.dto.responseDto.CourierResponseDto;
import org.example.blps.dto.responseDto.ResponsePaginationDto;
import org.example.blps.entity.Admin;
import org.example.blps.entity.Courier;
import org.example.blps.entity.User;
import org.example.blps.enums.CourierAccountState;
import org.example.blps.enums.CourierStatus;
import org.example.blps.mapper.CourierMapper;
import org.example.blps.repository.CourierRepository;
import org.example.blps.utils.PaginationUtil;
import org.example.blps.utils.ParseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    @isApprovedCourier
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
        return courierRepository.findByUserId(userService.findByEmail(email).getId()).orElseThrow(() -> new IllegalStateException("Курьера с таким email не существует!"));
    }

    public Courier findCourierWithOnlineStatus() {
        return courierRepository.findFirstByStatus(CourierStatus.ON_SHIFT).orElse(null);
    }

    public void saveCourier(Courier courier) {
        courierRepository.save(courier);
    }


    @isApprovedAdmin
    public Courier blockCourier(String email, Long id){
        Admin admin = adminService.findByUserId(userService.findByEmail(email).getId());
        Courier courier = courierRepository.findById(id).orElseThrow(
                ()->new IllegalStateException("Курьера с данным id не существует"));
        if (courier.getAccountState()==CourierAccountState.BLOCKED){
            throw new IllegalStateException("Курьер уже был заблокирован");
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
    @isApprovedAdmin
    public ResponsePaginationDto<CourierResponseDto> getAll(String page, String size,String courierState, String courierStatus){
        PaginationUtil.Params params = PaginationUtil.parse(page,size);
        CourierAccountState accountState = ParseUtil.parseEnum(courierState, CourierAccountState.class);
        CourierStatus status = ParseUtil.parseEnum(courierStatus, CourierStatus.class);
        Pageable pageable = PageRequest.of((int) params.page(),(int) params.size(), Sort.by("id").ascending());
        List<CourierResponseDto> result = new ArrayList<>();
        Page<Courier> couriers= courierRepository.findWithfilters(pageable,status,accountState);
        Long totalElements = courierRepository.count();
        for (Courier cr: couriers.getContent())
            result.add(mapper.fromEntityToDto(cr));
        return PaginationUtil.responsePaginationDto(result, params, totalElements);
    }
}

