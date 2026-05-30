package org.example.blps.worker;

import jakarta.persistence.EntityNotFoundException;
import org.example.blps.entity.Courier;
import org.example.blps.entity.Order;
import org.example.blps.entity.OrderAttempt;
import org.example.blps.enums.CourierStatus;
import org.example.blps.enums.OrderAttemptStatus;
import org.example.blps.enums.OrderStatus;
import org.example.blps.repository.OrderRepository;
import org.jboss.logging.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class OrderRefreshService {
    private static final Logger LOG = Logger.getLogger(OrderRefreshService.class);
    private final Integer LIMIT = 3;
    private final OrderAttemptService orderAttemptService;
    private final OrderRepository orderRepository;
    private final CourierFindService courierFindService;
    public OrderRefreshService(OrderRepository orderRepository, CourierFindService courierConsumer,
                               OrderAttemptService orderAttemptService){
        this.orderRepository = orderRepository;
        this.courierFindService = courierConsumer;
        this.orderAttemptService = orderAttemptService;
    }
    @Transactional
    public void refreshWaitingOrder(Long id){
        Order order = orderRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Заказа с данным id не существует"));
        if (order.getWaitingCycles() + orderAttemptService.countAttemptsForOrder(order) >= LIMIT) {
            order.setStatus(OrderStatus.FAILED);
            LOG.infov("Order {0} moved to FAILED: waitingCycles={1}", order.getId(), order.getWaitingCycles());
            return;
        }
        Courier courier = courierFindService.findOnlineCourier(orderAttemptService.findCouriersIdByOrder(order));
        if (courier == null){
            order.setWaitingCycles(order.getWaitingCycles()+1);
            if (order.getWaitingCycles() + orderAttemptService.countAttemptsForOrder(order) >= LIMIT) {
                order.setStatus(OrderStatus.FAILED);
            } else {
                order.setStatus(OrderStatus.WAITING);
            }
            LOG.infov("Order {0} has no available courier: status={1}, waitingCycles={2}",
                    order.getId(), order.getStatus(), order.getWaitingCycles());
            return;
        }
        order.setCourier(courier);
        order.setStatus(OrderStatus.PENDING);
        courier.setStatus(CourierStatus.ACCEPTING_ORDER);
        orderAttemptService.addOrderAttempt(courier, order, OrderAttemptStatus.ASSIGNED);
        LOG.infov("Order {0} assigned to courier {1}: status={2}, waitingCycles={3}",
                order.getId(), courier.getId(), order.getStatus(), order.getWaitingCycles());
    }

    @Transactional
    public void refreshAssignedOrder(Long id){
        OrderAttempt orderAttempt = orderAttemptService.findById(id);
        Order order = orderAttempt.getOrder();
        if (order.getStatus() == OrderStatus.PENDING){
            Courier oldCourier = orderAttempt.getCourier();
            if (oldCourier != null)
                changeCourier(order, oldCourier, OrderAttemptStatus.EXPIRED);
        }
    }

    public void changeCourier(Order order, Courier courier, OrderAttemptStatus status) {
        order.setCourier(null);
        if (courier.getStatus()!=CourierStatus.END_SHIFT) courier.setStatus(CourierStatus.ON_SHIFT);
        else courier.setStatus(CourierStatus.OFF_SHIFT);
        orderAttemptService.changeAttemptStatus(courier, order,status);
        if (order.getWaitingCycles()+ orderAttemptService.countAttemptsForOrder(order)>=LIMIT){
            order.setStatus(OrderStatus.FAILED);
            LOG.infov("Order {0} moved to FAILED after courier change: waitingCycles={1}",
                    order.getId(), order.getWaitingCycles());
            return;
        }
        Courier newCourier = courierFindService.findOnlineCourier(orderAttemptService.findCouriersIdByOrder(order));
        if (newCourier == null) {
            order.setStatus(OrderStatus.WAITING);
            LOG.infov("Order {0} returned to WAITING after courier change: waitingCycles={1}", order.getId(), order.getWaitingCycles());
            return;
        }
        orderAttemptService.addOrderAttempt(newCourier, order, OrderAttemptStatus.ASSIGNED);
        order.setCourier(newCourier);
        order.setStatus(OrderStatus.PENDING);
        newCourier.setStatus(CourierStatus.ACCEPTING_ORDER);
        LOG.infov("Order {0} reassigned to courier {1}: status={2}, waitingCycles={3}",
                order.getId(), newCourier.getId(), order.getStatus(), order.getWaitingCycles());
    }
}
