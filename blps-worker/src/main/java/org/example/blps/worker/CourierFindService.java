package org.example.blps.worker;

import org.example.blps.entity.Courier;
import org.example.blps.enums.CourierStatus;
import org.example.blps.repository.CourierRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourierFindService {
    private CourierRepository courierRepository;
    public CourierFindService(CourierRepository courierRepository) {
        this.courierRepository=courierRepository;
    }

    public Courier findOnlineCourier(List<Long> declinedCouriers){
        if (declinedCouriers.isEmpty()) return courierRepository.findFirstByStatus(CourierStatus.ON_SHIFT).orElse(null);
        return courierRepository.findFirstByStatusAndIdNotIn(CourierStatus.ON_SHIFT, declinedCouriers).orElse(null);
    }
}
