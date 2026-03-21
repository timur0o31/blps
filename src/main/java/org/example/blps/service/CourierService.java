//package org.example.blps.service;
//import org.example.blps.dto.requestDto.CourierRequestDto;
//import org.example.blps.mapper.CourierMapper;
//import org.example.blps.repository.CourierRepository;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//@Service
//public class CourierService {
//
//    private final CourierRepository courierRepository;
//    private final CourierMapper courierMapper;
//
//    @Autowired
//    public CourierService(CourierRepository courierRepository, CourierMapper courierMapper) {
//        this.courierRepository = courierRepository;
//        this.courierMapper = courierMapper;
//    }
//
//    public void addCourier(CourierRequestDto courierRequestDto) {
//        courierRepository.save(courierMapper.fromDtoToEntity(courierRequestDto));
//    }
//}
//
