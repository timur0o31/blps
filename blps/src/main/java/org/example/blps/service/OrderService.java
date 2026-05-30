package org.example.blps.service;
import jakarta.persistence.EntityNotFoundException;
import jakarta.resource.ResourceException;
import org.example.bitrix24.api.OrderConnection;
import org.example.bitrix24.api.OrderConnectionFactory;
import org.example.bitrix24.dto.ResourceOrderDto;
import org.example.bitrix24.dto.ResourceOrderStatus;
import org.example.blps.annotations.isApprovedCourier;
import org.example.blps.annotations.isApprovedCourierProcess;
import org.example.blps.dto.responseDto.ResponsePaginationDto;
import org.example.blps.entity.*;
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
    private final Integer LIMIT = 3;
    private final isApprovedCourierProcess isApprovedCourierProcess;
    private final OrderConnectionFactory orderConnectionFactory;


    @Autowired
    public OrderService(OrderRepository orderRepository, OrderMapper orderMapper,
                        UserService userService, ClientService clientService,
                        OrderAttemptService orderAttemptService,
                        CourierService courierService, CourierRepository courierRepository, isApprovedCourierProcess isApprovedCourierProcess,
                        OrderConnectionFactory orderConnectionFactory) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.courierService = courierService;
        this.userService = userService;
        this.clientService = clientService;
        this.orderAttemptService = orderAttemptService;
        this.isApprovedCourierProcess = isApprovedCourierProcess;
        this.orderConnectionFactory = orderConnectionFactory;
    }

    // Получить активные заказы (для курьера)
    @isApprovedCourier
    public OrderResponseDto getOrder(String email){
        Courier courier = courierService.findCourierByEmail(email);
        return orderMapper.fromEntityToDto(orderRepository.findByCourierAndStatus(courier, OrderStatus.PENDING));
    }

    @Transactional
    public OrderResponseDto addOrder(String email, OrderRequestDto orderRequestDto) {
            Order newOrder = orderMapper.fromDtoToEntity(orderRequestDto);
            Client client = clientService.findByUser(userService.findByEmail(email));
            newOrder.setClient(client);
            Courier courier = courierService.findCourierWithOnlineStatus();
            if  (courier == null) {
                newOrder.setStatus(OrderStatus.WAITING);
               // throw new RuntimeException("тест отката транзакции");
                Order savedOrder = orderRepository.save(newOrder);
                saveOrderToBitrix(savedOrder);
                return orderMapper.fromEntityToDto(savedOrder);
            }
            newOrder.setCourier(courier);
            newOrder.setStatus(OrderStatus.PENDING);

            Order savedOrder = orderRepository.save(newOrder);
            saveOrderToBitrix(savedOrder);
            orderAttemptService.addOrderAttempt(courier,newOrder, OrderAttemptStatus.ASSIGNED);
            courier.setStatus(CourierStatus.ACCEPTING_ORDER);
            //throw new RuntimeException("тест отката транзакции");
            return orderMapper.fromEntityToDto(newOrder);
    }
    // Обновить заказ (для курьера)

    @Transactional
    @isApprovedCourier
    public OrderResponseDto updateOrder(Long id, OrderStatusRequestDto orderRequestDto, String email) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Заказа с данным id не существует"));
        Courier courier = courierService.findCourierByEmail(email);
        validateOrder(order);
        if (!order.getCourier().getId().equals(courier.getId())){
            throw new AccessDeniedException("Другой курьер не может менять статус заказа");
        }
        OrderStatus prevOrderStatus = order.getStatus();
        if (!prevOrderStatus.canSwitchTo(orderRequestDto.getOrderStatus())){
            throw new IllegalStateException("Из состояния "+prevOrderStatus +" нельзя перейти в "+ orderRequestDto.getOrderStatus());
        }
        order.setStatus(orderRequestDto.getOrderStatus());
        updateOrderInBitrix(order);
        if (orderRequestDto.getOrderStatus()==OrderStatus.DELIVERED){
            if (courier.getStatus()==CourierStatus.END_SHIFT){
               courier.setStatus(CourierStatus.OFF_SHIFT);
            }else {
                courier.setStatus(CourierStatus.ON_SHIFT);
                courierService.saveCourier(courier);
            }
        }
        return orderMapper.fromEntityToDto(orderRepository.save(order));
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

    private void validateOrder(Order order){
        if (order.getCourier() == null){
            if (order.getStatus()==OrderStatus.FAILED)
                throw new IllegalStateException("Такой заказ уже был завершен!");
            else if (order.getStatus() == OrderStatus.WAITING)
                throw new AccessDeniedException("Данный заказ не назначен вам!");
        }
    }
    @Transactional
    @isApprovedCourier
    public void cancelOrderById(Long orderId, String email) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new EntityNotFoundException("Заказ не найден"));
        Courier courier = courierService.findCourierByEmail(email);
        validateOrder(order);
        if (!order.getCourier().getId().equals(courier.getId())) throw new AccessDeniedException("Курьер не может отменять чужие заказы");
        changeCourier(order, courier, OrderAttemptStatus.REJECTED);
    }

    private void changeCourier(Order order, Courier courier, OrderAttemptStatus status) {
        order.setCourier(null);
        if (courier.getStatus()!=CourierStatus.END_SHIFT) courier.setStatus(CourierStatus.ON_SHIFT);
        else courier.setStatus(CourierStatus.OFF_SHIFT);
        orderAttemptService.changeAttemptStatus(courier, order,status);
        if (order.getWaitingCycles()+orderAttemptService.countAttemptsForOrder(order)>=LIMIT){
            order.setStatus(OrderStatus.FAILED);
            updateOrderInBitrix(order);
            return;
        }
        Courier newCourier = courierService.findOnlineCourier(orderAttemptService.findCouriersIdByOrder(order));
        if (newCourier == null) {
            order.setStatus(OrderStatus.WAITING);
            updateOrderInBitrix(order);
            return;
        }
        orderAttemptService.addOrderAttempt(newCourier, order, OrderAttemptStatus.ASSIGNED);
        order.setCourier(newCourier);
        order.setStatus(OrderStatus.PENDING);
        updateOrderInBitrix(order);
        newCourier.setStatus(CourierStatus.ACCEPTING_ORDER);
    }

    @Transactional
    @isApprovedCourier
    public void acceptOrderByCourierId(Long orderId, String email) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new EntityNotFoundException("Заказ не найден"));
        Courier courier = courierService.findCourierByEmail(email);
        validateOrder(order);
        if (!order.getCourier().getId().equals(courier.getId())) throw new AccessDeniedException("Курьер не может принимать чужие заказы!");
        if (order.getStatus() != OrderStatus.PENDING && order.getCourier().getId().equals(courier.getId()))
            throw new IllegalStateException("Вы уже приняли данный заказ");
        if (order.getStatus()!=OrderStatus.PENDING)
            throw new IllegalStateException("Только из состояния PENDING можно принять заказ!");
        order.setStatus(OrderStatus.ACCEPTED);
        updateOrderInBitrix(order);
        if (courier.getStatus()!=CourierStatus.END_SHIFT) courier.setStatus(CourierStatus.BUSY);
        else courier.setStatus(CourierStatus.END_SHIFT);
        orderAttemptService.changeAttemptStatus(courier, order, OrderAttemptStatus.ACCEPTED);
        orderRepository.save(order);
    }

    public List<Long> getTop10WaitingOrders(){
        return orderRepository.findTop10ByStatus(OrderStatus.WAITING)
                .stream().map((Order order)-> order.getId()).toList();
    }
    @Transactional
    public void refreshWaitingOrder(Long id){
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Заказа с данным id не существует"));
        if (order.getWaitingCycles() + orderAttemptService.countAttemptsForOrder(order) >= LIMIT) {
            order.setStatus(OrderStatus.FAILED);
            updateOrderInBitrix(order);
            return;
        }
        Courier courier = courierService.findOnlineCourier(orderAttemptService.findCouriersIdByOrder(order));
        if (courier == null){
            order.setWaitingCycles(order.getWaitingCycles()+1);
            if (order.getWaitingCycles() + orderAttemptService.countAttemptsForOrder(order) >= LIMIT) order.setStatus(OrderStatus.FAILED);
            else order.setStatus(OrderStatus.WAITING);
            updateOrderInBitrix(order);
            return;
        }
        order.setCourier(courier);
        order.setStatus(OrderStatus.PENDING);
        updateOrderInBitrix(order);
        courier.setStatus(CourierStatus.ACCEPTING_ORDER);
        orderAttemptService.addOrderAttempt(courier, order, OrderAttemptStatus.ASSIGNED);
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

    private void saveOrderToBitrix(Order order) {
        try (OrderConnection connection = orderConnectionFactory.getConnection()) {
            ResourceOrderDto resourceOrderDto = new ResourceOrderDto(
                    order.getId(),
                    order.getAddress(),
                    order.getContent(),
                    ResourceOrderStatus.valueOf(order.getStatus().name())
            );

            connection.createOrder(resourceOrderDto);
        } catch (ResourceException e) {
            throw new RuntimeException("Ошибка при сохранении заказа в Bitrix24", e);
        }
    }

    private void updateOrderInBitrix(Order order) {
        try (OrderConnection connection = orderConnectionFactory.getConnection()) {
            ResourceOrderDto resourceOrderDto = new ResourceOrderDto(
                    order.getId(),
                    order.getAddress(),
                    order.getContent(),
                    ResourceOrderStatus.valueOf(order.getStatus().name())
            );
            connection.updateOrder(resourceOrderDto);
        } catch (ResourceException e) {
            throw new RuntimeException("Ошибка при обновлении заказа в Bitrix24", e);
        }
    }

    @Transactional
    public void updateOrderFromBitrix(Long backendId, String status) {
        Order order = orderRepository.findById(backendId).orElseThrow(() -> new EntityNotFoundException("Заказ с таким id не найден!"));
        order.setStatus(OrderStatus.valueOf(status));
        orderRepository.save(order);
    }

}
