package org.example.blps.worker;

import jakarta.persistence.EntityNotFoundException;
import jakarta.resource.ResourceException;
import org.example.bitrix24.api.OrderConnection;
import org.example.bitrix24.api.OrderConnectionFactory;
import org.example.bitrix24.dto.ResourceOrderDto;
import org.example.bitrix24.dto.ResourceOrderStatus;
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
    private final OrderConnectionFactory orderConnectionFactory;
    public OrderRefreshService(OrderRepository orderRepository, CourierFindService courierConsumer,
                               OrderAttemptService orderAttemptService,  OrderConnectionFactory orderConnectionFactory) {
        this.orderRepository = orderRepository;
        this.courierFindService = courierConsumer;
        this.orderAttemptService = orderAttemptService;
        this.orderConnectionFactory = orderConnectionFactory;
    }
    @Transactional
    public void refreshWaitingOrder(Long id){
        Order order = orderRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Заказа с данным id не существует"));
        if (order.getWaitingCycles() + orderAttemptService.countAttemptsForOrder(order) >= LIMIT) {
            order.setStatus(OrderStatus.FAILED);
            //updateOrderInBitrix(order);
            LOG.infov("Заказ {0} перемещен в FAILED: waitingCycles={1}", order.getId(), order.getWaitingCycles());
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
            //updateOrderInBitrix(order);
            LOG.infov("Заказ {0} не может найти доступных курьеров: status={1}, waitingCycles={2}",
                    order.getId(), order.getStatus(), order.getWaitingCycles());
            return;
        }
        order.setCourier(courier);
        order.setStatus(OrderStatus.PENDING);
        //updateOrderInBitrix(order);
        courier.setStatus(CourierStatus.ACCEPTING_ORDER);
        orderAttemptService.addOrderAttempt(courier, order, OrderAttemptStatus.ASSIGNED);
        LOG.infov("Заказ {0} назначен курьеру {1}: status={2}, waitingCycles={3}",
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

    private void changeCourier(Order order, Courier courier, OrderAttemptStatus status) {
        order.setCourier(null);
        if (courier.getStatus()!=CourierStatus.END_SHIFT) courier.setStatus(CourierStatus.ON_SHIFT);
        else courier.setStatus(CourierStatus.OFF_SHIFT);
        orderAttemptService.changeAttemptStatus(courier, order,status);
        if (order.getWaitingCycles()+ orderAttemptService.countAttemptsForOrder(order)>=LIMIT){
            order.setStatus(OrderStatus.FAILED);
            //updateOrderInBitrix(order);
            LOG.infov("Заказ {0} перевод в FAILED: waitingCycles={1}",
                    order.getId(), order.getWaitingCycles());
            return;
        }
        Courier newCourier = courierFindService.findOnlineCourier(orderAttemptService.findCouriersIdByOrder(order));
        if (newCourier == null) {
            order.setStatus(OrderStatus.WAITING);
          //  updateOrderInBitrix(order);
            LOG.infov("Заказ {0} возврат к WAITING: waitingCycles={1}", order.getId(), order.getWaitingCycles());
            return;
        }
        orderAttemptService.addOrderAttempt(newCourier, order, OrderAttemptStatus.ASSIGNED);
        order.setCourier(newCourier);
        order.setStatus(OrderStatus.PENDING);
        //updateOrderInBitrix(order);
        newCourier.setStatus(CourierStatus.ACCEPTING_ORDER);
        LOG.infov("Заказ {0} переназначение курьеру {1}: status={2}, waitingCycles={3}",
                order.getId(), newCourier.getId(), order.getStatus(), order.getWaitingCycles());
    }
    private void updateOrderInBitrix(Order order) {
        LOG.infov("Пересылка заказа {0} статус {1} в Bitrix24", order.getId(), order.getStatus());
        try (OrderConnection connection = orderConnectionFactory.getConnection()) {
            LOG.infov("Bitrix24 попытка обновить {0}: {1}", order.getId(), connection.getClass().getName());
            ResourceOrderDto resourceOrderDto = new ResourceOrderDto(
                    order.getId(),
                    order.getAddress(),
                    order.getContent(),
                    ResourceOrderStatus.valueOf(order.getStatus().name())
            );
            connection.updateOrder(resourceOrderDto);
            LOG.infov(" Заказ {0} статус {1} пересылка в Bitrix24", order.getId(), order.getStatus());
        } catch (ResourceException e) {
            throw new RuntimeException("Ошибка при обновлении заказа в Bitrix24", e);
        } catch (LinkageError e) {
            throw e;
        } catch (RuntimeException e) {
            throw e;
        }
    }
}
