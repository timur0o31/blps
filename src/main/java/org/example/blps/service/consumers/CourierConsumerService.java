package org.example.blps.service.consumers;

import org.example.blps.entity.Courier;
import org.example.blps.enums.CourierStatus;
import org.example.blps.repository.CourierRepository;
import org.example.blps.service.consumers.Repository.CourierConsumer;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
public class CourierConsumerService {
    private CourierConsumer courierConsumer;
    public CourierConsumerService(CourierConsumer courierConsumer) {
        this.courierConsumer = courierConsumer;
    }
    public Courier findCourierWithOnlineStatus() {
        return courierConsumer.findFirstByStatus(CourierStatus.ON_SHIFT).orElse(null);
    }
    public Courier findOnlineCourier(List<Long> declinedCouriers){
        if (declinedCouriers.isEmpty()) return courierConsumer.findFirstByStatus(CourierStatus.ON_SHIFT).orElse(null);
        return courierConsumer.findFirstByStatusAndIdNotIn(CourierStatus.ON_SHIFT, declinedCouriers).orElse(null);
    }
}
