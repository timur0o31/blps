package org.example.blps.service;

import org.example.blps.dto.responseDto.CourierApplicationsResponseDto;
import org.example.blps.dto.responseDto.ResponsePaginationDto;
import org.example.blps.entity.Admin;
import org.example.blps.entity.Courier;
import org.example.blps.entity.CourierRequest;
import org.example.blps.enums.CourierAccountState;
import org.example.blps.enums.CourierRequestStatus;
import org.example.blps.mapper.CourierRequestMapper;
import org.example.blps.repository.CourierRequestRepository;
import org.example.blps.utils.PaginationUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

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
        if (courier.getAccountState()==CourierAccountState.BLOCKED){
            throw new RuntimeException("Заблокированным сотрудникам нельзя трудоустроиться");
        }
        if (!courierRequestRepository.findCourierRequestByCourierAndStatus(courier, CourierRequestStatus.APPROVED).isEmpty()){
            throw new RuntimeException("Заявка уже одобрена");
        }
        if (!courierRequestRepository.findCourierRequestByCourierAndStatus(courier, CourierRequestStatus.PENDING).isEmpty()){
            throw new RuntimeException("Заявка на трудоустройство уже подана");
        }
        CourierRequest courierRequest = new CourierRequest();
        courierRequest.setCourier(courier);
        courierRequest.setStatus(CourierRequestStatus.PENDING);
        courierRequestRepository.save(courierRequest);
    }
    @Transactional
    public void approveRequest(String email, Long id){
        Admin admin = adminService.findByUserId(userService.findByEmail(email).getId());
        CourierRequest courierRequest = courierRequestRepository.findCourierRequestById(id)
                .orElseThrow(()->new RuntimeException("заявки с данным id не существует"));
        if (courierRequest.getStatus()!=CourierRequestStatus.PENDING) throw new RuntimeException("Заявку можно одобрить только из состояния ожидания!");
        courierRequest.setStatus(CourierRequestStatus.APPROVED);
        courierRequest.setReviewedBy(admin);
        Courier courier = courierRequest.getCourier();
        courier.setAccountState(CourierAccountState.ACTIVE);
        courierRequestRepository.save(courierRequest);
        courierService.saveCourier(courier);
    }
    public void declineRequest(String email, Long id){
        Admin admin = adminService.findByUserId(userService.findByEmail(email).getId());
        CourierRequest courierRequest = courierRequestRepository.findCourierRequestById(id)
                .orElseThrow(()->new RuntimeException("заявки с данным id не существует"));
        if (courierRequest.getStatus()!=CourierRequestStatus.PENDING) throw new RuntimeException("Заявку можно отклонить только из состояния ожидания!");
        courierRequest.setStatus(CourierRequestStatus.DECLINED);
        courierRequest.setReviewedBy(admin);
        courierRequestRepository.save(courierRequest);
    }
    //доделать пагинацию
    public ResponsePaginationDto<CourierApplicationsResponseDto> getAll(String page, String size){
        PaginationUtil.Params params = PaginationUtil.parse(page,size);
        List<CourierApplicationsResponseDto> result = new ArrayList<>();
        List<CourierRequest> courierRequests = courierRequestRepository.findCourierRequestByStatus(CourierRequestStatus.PENDING);
        Long totalElements = courierRequestRepository.countAllByStatus(CourierRequestStatus.PENDING);
        for (CourierRequest cr: courierRequests){
            result.add(mapper.fromEntityToDto(cr,userService.findById(cr.getCourier().getUserId())));
        }
        return PaginationUtil.responsePaginationDto(result, params, totalElements);
    }
}
