package org.example.blps.service;

import org.example.blps.annotations.isApprovedAdmin;
import org.example.blps.dto.responseDto.CourierApplicationsResponseDto;
import org.example.blps.dto.responseDto.ResponsePaginationDto;
import org.example.blps.entity.Admin;
import org.example.blps.entity.Courier;
import org.example.blps.entity.CourierRequest;
import org.example.blps.enums.CourierAccountState;
import org.example.blps.enums.CourierRequestStatus;
import org.example.blps.enums.CourierStatus;
import org.example.blps.mapper.CourierRequestMapper;
import org.example.blps.repository.CourierRequestRepository;
import org.example.blps.utils.PaginationUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourierRequestService {
    private final CourierService courierService;
    private final CourierRequestRepository courierRequestRepository;
    private final AdminService adminService;
    private final CourierRequestMapper mapper;
    private final UserService userService;
    public CourierRequestService(CourierService courierService, CourierRequestRepository courierRequestRepository, AdminService adminService, CourierRequestMapper mapper,
        UserService userService){
        this.courierService = courierService;
        this.courierRequestRepository = courierRequestRepository;
        this.adminService = adminService;
        this.mapper = mapper;
        this.userService = userService;
    }

    public void submitRequest(String email) {
        Courier courier = courierService.findCourierByEmail(email);
        if (courier.getAccountState()==CourierAccountState.BLOCKED)
            throw new AccessDeniedException("Заблокированным сотрудникам нельзя трудоустроиться");
        if (!courierRequestRepository.findCourierRequestByCourierAndStatus(courier, CourierRequestStatus.APPROVED).isEmpty())
            throw new IllegalStateException("Заявка уже одобрена");
        if (!courierRequestRepository.findCourierRequestByCourierAndStatus(courier, CourierRequestStatus.PENDING).isEmpty())
            throw new IllegalStateException("Заявка на трудоустройство уже подана");
        CourierRequest courierRequest = new CourierRequest();
        courierRequest.setCourier(courier);
        courierRequest.setStatus(CourierRequestStatus.PENDING);
        courierRequestRepository.save(courierRequest);
    }

    @Transactional
    @isApprovedAdmin
    public void approveRequest(String email, Long id){
        Admin admin = adminService.findByUserId(userService.findByEmail(email).getId());
        CourierRequest courierRequest = courierRequestRepository.findCourierRequestById(id)
                .orElseThrow(()->new IllegalStateException("заявки с данным id не существует"));
        if (courierRequest.getStatus()!=CourierRequestStatus.PENDING) throw new IllegalStateException("Заявку можно одобрить только из состояния ожидания!");
        courierRequest.setStatus(CourierRequestStatus.APPROVED);
        courierRequest.setReviewedBy(admin);
        Courier courier = courierRequest.getCourier();
        courier.setAccountState(CourierAccountState.ACTIVE);
        courierRequestRepository.save(courierRequest);
        courierService.saveCourier(courier);
    }
    @isApprovedAdmin
    public void declineRequest(String email, Long id){
        Admin admin = adminService.findByUserId(userService.findByEmail(email).getId());
        CourierRequest courierRequest = courierRequestRepository.findCourierRequestById(id)
                .orElseThrow(()->new IllegalStateException("заявки с данным id не существует"));
        if (courierRequest.getStatus()!=CourierRequestStatus.PENDING) throw new IllegalStateException("Заявку можно отклонить только из состояния ожидания!");
        courierRequest.setStatus(CourierRequestStatus.DECLINED);
        courierRequest.setReviewedBy(admin);
        courierRequestRepository.save(courierRequest);
    }

    @isApprovedAdmin
    public ResponsePaginationDto<CourierApplicationsResponseDto> getAll(String page, String size, Long courierId, String status){
        CourierRequestStatus parsedStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                parsedStatus = CourierRequestStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                String enumVal = Arrays.stream(CourierStatus.values())
                        .map(em -> em.name())
                        .collect(Collectors.joining("; "));
                throw new IllegalArgumentException("Некорректный статус заявки: " + status + ". Допустимые значения = "+enumVal);
            }
        }
        PaginationUtil.Params params = PaginationUtil.parse(page,size);
        List<CourierApplicationsResponseDto> result = new ArrayList<>();
        Pageable pageAble = PageRequest.of((int) params.page(), (int) params.size(), Sort.by("id").ascending());
        Page<CourierRequest> courierRequests = courierRequestRepository.findWithfilters(pageAble, parsedStatus, courierId);
        long totalElements = courierRequests.getTotalElements();
        for (CourierRequest cr: courierRequests.getContent())
            result.add(mapper.fromEntityToDto(cr,userService.findById(cr.getCourier().getUserId())));
        return PaginationUtil.responsePaginationDto(result, params, totalElements);
    }
}
