package org.example.blps.service;
import jakarta.persistence.EntityNotFoundException;
import org.example.blps.annotations.isApprovedCourier;
import org.example.blps.annotations.isApprovedCourierProcess;
import org.example.blps.dto.responseDto.ResponsePaginationDto;
import org.example.blps.repository.CourierRepository;
import org.example.blps.utils.PaginationUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;
import org.example.blps.dto.requestDto.OrderRequestDto;
import org.example.blps.dto.requestDto.OrderStatusRequestDto;
import org.example.blps.dto.responseDto.OrderResponseDto;
import org.example.blps.entity.*;
import org.example.blps.enums.OrderAttemptStatus;
import org.example.blps.mapper.OrderMapper;
import org.example.blps.repository.OrderRepository;
import org.example.blps.enums.CourierStatus;
import org.example.blps.enums.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final UserService userService;
    private final ClientService clientService;
    private final OrderAttemptService orderAttemptService;
    private final CourierService courierService;
    private final CourierRepository courierRepository;
    private final Integer LIMIT = 3;
    private final org.example.blps.annotations.isApprovedCourierProcess isApprovedCourierProcess;

    @Autowired
    public OrderService(OrderRepository orderRepository, OrderMapper orderMapper,
                        UserService userService, ClientService clientService,
                        OrderAttemptService orderAttemptService,
                        CourierService courierService, CourierRepository courierRepository, isApprovedCourierProcess isApprovedCourierProcess) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.courierService = courierService;
        this.courierRepository = courierRepository;
        this.userService = userService;
        this.clientService = clientService;
        this.orderAttemptService = orderAttemptService;
        this.isApprovedCourierProcess = isApprovedCourierProcess;
    }

    // Получить активные заказы (для курьера)
    public OrderResponseDto getOrder(String email){
        Courier courier = courierService.findCourierByEmail(email);
        return orderMapper.fromEntityToDto(orderRepository.findByCourierAndStatus(courier, OrderStatus.PENDING));
    }

    @Transactional
    public OrderResponseDto addOrder(String email, OrderRequestDto dto) {
        Order order = orderMapper.fromDtoToEntity(dto);
        Client client = clientService.findByUser(userService.findByEmail(email));
        order.setClient(client);
        order.setStatus(OrderStatus.NEW);
        return orderMapper.fromEntityToDto(orderRepository.save(order));
    }

    @Transactional
    public boolean isAssignmentLimitReached(Long orderId) {
        Order order = findOrderById(orderId);
        return order.getWaitingCycles() + orderAttemptService.countAttemptsForOrder(order) >= LIMIT;
    }

    @Transactional
    public Long findAvailableCourier(Long orderId) {
        Order order = findOrderById(orderId);
        List<Long> excludedCouriers = orderAttemptService.findCouriersIdByOrder(order);
        Courier courier = courierService.findOnlineCourier(excludedCouriers);
        if (courier == null) {
            return null;
        } else {
            return courier.getId();
        }
    }

    @Transactional
    public Long createAssignmentAttempt(Long orderId, Long courierId) {
        Order order = findOrderById(orderId);
        Courier courier = findCourierById(courierId);
        OrderAttempt attempt = orderAttemptService.addOrderAttempt(courier, order, OrderAttemptStatus.ASSIGNED);
        return attempt.getId();
    }

    @Transactional
    public void assignCourierToOrder(Long orderId, Long courierId) {
        Order order = findOrderById(orderId);
        Courier courier = findCourierById(courierId);
        if (courier.getStatus() != CourierStatus.ON_SHIFT) {
            throw new IllegalStateException("Курьер уже недоступен для назначения");
        }
        order.setCourier(courier);
        order.setStatus(OrderStatus.PENDING);
    }

    @Transactional
    public void markCourierAsAccepting(Long courierId) {
        Courier courier = findCourierById(courierId);
        courier.setStatus(CourierStatus.ACCEPTING_ORDER);
    }

    @Transactional
    public void markOrderWaiting(Long orderId) {
        Order order = findOrderById(orderId);
        order.setWaitingCycles(order.getWaitingCycles() + 1);
        order.setStatus(OrderStatus.WAITING);
    }

    @Transactional
    public void markOrderFailed(Long orderId) {
        Order order = findOrderById(orderId);
        order.setStatus(OrderStatus.FAILED);
    }

    @Transactional
    public void ensureOrderAssignedToCourier(Long orderId, String email) {
        Order order = findOrderById(orderId);
        Courier courier = courierService.findCourierByEmail(email);
        if (order.getCourier() == null || !order.getCourier().getId().equals(courier.getId())) {
            throw new AccessDeniedException("Курьер не может управлять чужим заказом");
        }
    }

    @Transactional
    public void cancelOrderById(Long orderId, Long courierId) {
        Order order = findOrderById(orderId);
        Courier courier = findCourierById(courierId);
        validateAssignedCourier(order, courier);
        orderAttemptService.changeAttemptStatus(courier, order, OrderAttemptStatus.REJECTED);
        order.setCourier(null);
        order.setStatus(OrderStatus.WAITING);
        courier.setStatus(courier.getStatus() == CourierStatus.END_SHIFT ? CourierStatus.OFF_SHIFT : CourierStatus.ON_SHIFT);
    }

    @Transactional
    public void acceptOrderByCourierId(Long orderId, Long courierId) {
        Order order = findOrderById(orderId);
        Courier courier = findCourierById(courierId);
        validateAssignedCourier(order, courier);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Принять можно только заказ в статусе PENDING");
        }
        order.setStatus(OrderStatus.ACCEPTED);
        if (courier.getStatus() != CourierStatus.END_SHIFT) {
            courier.setStatus(CourierStatus.BUSY);
        }
        orderAttemptService.changeAttemptStatus(courier, order, OrderAttemptStatus.ACCEPTED);
    }

    @Transactional
    public boolean updateOrder(Long orderId, Long courierId, OrderStatus nextStatus) {
        Order order = findOrderById(orderId);
        Courier courier = findCourierById(courierId);
        validateAssignedCourier(order, courier);
        if (!order.getStatus().canSwitchTo(nextStatus)) {
            throw new IllegalStateException("Из состояния " + order.getStatus() + " нельзя перейти в " + nextStatus);
        }
        order.setStatus(nextStatus);
        if (nextStatus == OrderStatus.DELIVERED) {
            courier.setStatus(courier.getStatus() == CourierStatus.END_SHIFT
                    ? CourierStatus.OFF_SHIFT
                    : CourierStatus.ON_SHIFT);
            return true;
        }
        return false;
    }

    private void validateAssignedCourier(Order order, Courier courier) {
        if (order.getCourier() == null || !order.getCourier().getId().equals(courier.getId())) {
            throw new AccessDeniedException("Заказ назначен другому курьеру");
        }
    }

    @Transactional
    public String getCamundaUserIdByCourierId(Long courierId) {
        Courier courier = findCourierById(courierId);
        return "user" + courier.getUserId();
    }


    // Истекшие попытки назначения
    @Transactional
    public void expireAssignment(Long attemptId) {
        OrderAttempt attempt = orderAttemptService.findById(attemptId);
        if (attempt.getStatus() != OrderAttemptStatus.ASSIGNED) {
            return;
        }
        Order order = attempt.getOrder();
        if (order.getStatus() != OrderStatus.PENDING) {
            return;
        }

        Courier courier = attempt.getCourier();
        attempt.setStatus(OrderAttemptStatus.EXPIRED);
        order.setCourier(null);
        order.setStatus(OrderStatus.WAITING);

        if (courier != null) {
            if (courier.getStatus() == CourierStatus.END_SHIFT) {
                courier.setStatus(CourierStatus.OFF_SHIFT);
            } else {
                courier.setStatus(CourierStatus.ON_SHIFT);
            }
        }
    }

    private Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Заказ не найден"));
    }
    private Courier findCourierById(Long courierId) {
        return courierRepository.findById(courierId)
                .orElseThrow(() -> new EntityNotFoundException("Курьер не найден"));
    }



    public ResponsePaginationDto getOrderHistory(String email, String page, String size) {
        List<OrderResponseDto> orderHistory = new ArrayList<>();
        User user = userService.findByEmail(email);
        Client client = clientService.findByUser(user);
        PaginationUtil.Params params = PaginationUtil.parse(page, size);
        Pageable pageable = PageRequest.of((int) params.page(),(int) params.size(), Sort.by("id").ascending());
        Page<Order> orders = orderRepository.findOrdersByClientId(client.getId(),pageable);
        Long totalElements = orderRepository.countOrderByClientId(client.getId());
        for (Order order:orders.getContent())
            orderHistory.add(orderMapper.fromEntityToDto(order));
        return PaginationUtil.responsePaginationDto(orderHistory, params, totalElements);
    }

    public OrderResponseDto getStatusOrder(Long id, String email){
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Заказа с данным id не существует"));
        Client client = clientService.findByUser(userService.findByEmail(email));
        if (!order.getClient().getId().equals(client.getId()))
            throw new AccessDeniedException("Клиент не может просматривать статус чужого заказа");
        return orderMapper.fromEntityToDto(order);
    }


//    @Transactional
//    public OrderResponseDto addOrder(String email, OrderRequestDto orderRequestDto) {
//            Order newOrder = orderMapper.fromDtoToEntity(orderRequestDto);
//            Client client = clientService.findByUser(userService.findByEmail(email));
//            newOrder.setClient(client);
//            Courier courier = courierService.findCourierWithOnlineStatus();
//            if  (courier == null) {
//                newOrder.setStatus(OrderStatus.WAITING);
//               // throw new RuntimeException("тест отката транзакции");
//                return orderMapper.fromEntityToDto(orderRepository.save(newOrder));
//            }
//            newOrder.setCourier(courier);
//            newOrder.setStatus(OrderStatus.PENDING);
//            orderRepository.save(newOrder);
//            orderAttemptService.addOrderAttempt(courier,newOrder, OrderAttemptStatus.ASSIGNED);
//            courier.setStatus(CourierStatus.ACCEPTING_ORDER);
//            //throw new RuntimeException("тест отката транзакции");
//            return orderMapper.fromEntityToDto(newOrder);
//    }



//    @Transactional
//    @isApprovedCourier
//    public OrderResponseDto updateOrder(Long id, OrderStatusRequestDto orderRequestDto, String email) {
//        Order order = orderRepository.findById(id)
//                .orElseThrow(() -> new EntityNotFoundException("Заказа с данным id не существует"));
//        Courier courier = courierService.findCourierByEmail(email);
//        validateOrder(order);
//        if (!order.getCourier().getId().equals(courier.getId())){
//            throw new AccessDeniedException("Другой курьер не может менять статус заказа");
//        }
//        OrderStatus prevOrderStatus = order.getStatus();
//        if (!prevOrderStatus.canSwitchTo(orderRequestDto.getOrderStatus())){
//            throw new IllegalStateException("Из состояния "+prevOrderStatus +" нельзя перейти в "+ orderRequestDto.getOrderStatus());
//        }
//        order.setStatus(orderRequestDto.getOrderStatus());
//        if (orderRequestDto.getOrderStatus()==OrderStatus.DELIVERED){
//            if (courier.getStatus()==CourierStatus.END_SHIFT){
//               courier.setStatus(CourierStatus.OFF_SHIFT);
//            }else {
//                courier.setStatus(CourierStatus.ON_SHIFT);
//                courierService.saveCourier(courier);
//            }
//        }
//        return orderMapper.fromEntityToDto(orderRepository.save(order));
//    }


//    private void validateOrder(Order order){
//        if (order.getCourier() == null){
//            if (order.getStatus()==OrderStatus.FAILED)
//                throw new IllegalStateException("Такой заказ уже был завершен!");
//            else if (order.getStatus() == OrderStatus.WAITING)
//                throw new AccessDeniedException("Данный заказ не назначен вам!");
//        }
//    }
//
//    @Transactional
//    @isApprovedCourier
//    public void cancelOrderById(Long orderId, String email) {
//        Order order = orderRepository.findById(orderId).orElseThrow(() -> new EntityNotFoundException("Заказ не найден"));
//        Courier courier = courierService.findCourierByEmail(email);
//        validateOrder(order);
//        if (!order.getCourier().getId().equals(courier.getId())) throw new AccessDeniedException("Курьер не может отменять чужие заказы");
//        changeCourier(order, courier, OrderAttemptStatus.REJECTED);
//    }
//
//    private void changeCourier(Order order, Courier courier, OrderAttemptStatus status) {
//        order.setCourier(null);
//        if (courier.getStatus()!=CourierStatus.END_SHIFT) courier.setStatus(CourierStatus.ON_SHIFT);
//        else courier.setStatus(CourierStatus.OFF_SHIFT);
//        orderAttemptService.changeAttemptStatus(courier, order,status);
//        if (order.getWaitingCycles()+orderAttemptService.countAttemptsForOrder(order)>=LIMIT){
//            order.setStatus(OrderStatus.FAILED);
//            return;
//        }
//        Courier newCourier = courierService.findOnlineCourier(orderAttemptService.findCouriersIdByOrder(order));
//        if (newCourier == null) {
//            order.setStatus(OrderStatus.WAITING);
//            return;
//        }
//        orderAttemptService.addOrderAttempt(newCourier, order, OrderAttemptStatus.ASSIGNED);
//        order.setCourier(newCourier);
//        order.setStatus(OrderStatus.PENDING);
//        newCourier.setStatus(CourierStatus.ACCEPTING_ORDER);
//    }

//    @Transactional
//    @isApprovedCourier
//    public void acceptOrderByCourierId(Long orderId, String email) {
//        Order order = orderRepository.findById(orderId).orElseThrow(() -> new EntityNotFoundException("Заказ не найден"));
//        Courier courier = courierService.findCourierByEmail(email);
//        validateOrder(order);
//        if (!order.getCourier().getId().equals(courier.getId())) throw new AccessDeniedException("Курьер не может принимать чужие заказы!");
//        if (order.getStatus() != OrderStatus.PENDING && order.getCourier().getId().equals(courier.getId()))
//            throw new IllegalStateException("Вы уже приняли данный заказ");
//        if (order.getStatus()!=OrderStatus.PENDING)
//            throw new IllegalStateException("Только из состояния PENDING можно принять заказ!");
//        order.setStatus(OrderStatus.ACCEPTED);
//        if (courier.getStatus()!=CourierStatus.END_SHIFT) courier.setStatus(CourierStatus.BUSY);
//        else courier.setStatus(CourierStatus.END_SHIFT);
//        orderAttemptService.changeAttemptStatus(courier, order, OrderAttemptStatus.ACCEPTED);
//        orderRepository.save(order);
//    }

}
