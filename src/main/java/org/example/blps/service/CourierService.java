package org.example.blps.service;
import org.example.blps.dto.requestDto.CourierRequstUpdateStatusDto;
import org.example.blps.entity.Courier;
import org.example.blps.repository.CourierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.stereotype.Service;

@Service
public class CourierService {

    private final CourierRepository courierRepository;

    @Autowired
    public CourierService(CourierRepository courierRepository) {
        this.courierRepository = courierRepository;
    }

    public Courier updateCourierStatus(Long id, CourierRequstUpdateStatusDto courierRequstUpdateStatusDto) {
        Courier courier = courierRepository.findById(id).orElseThrow(() -> new RuntimeException("Courier not found"));
        courier.setStatus(courierRequstUpdateStatusDto.getStatus());
        return courierRepository.save(courier);
    }
}

