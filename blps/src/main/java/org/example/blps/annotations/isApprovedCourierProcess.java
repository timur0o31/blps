package org.example.blps.annotations;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.example.blps.entity.Courier;
import org.example.blps.entity.User;
import org.example.blps.enums.CourierAccountState;
import org.example.blps.repository.CourierRepository;
import org.example.blps.repository.UserRepository;
import org.example.blps.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class isApprovedCourierProcess {

    private final UserRepository userRepository;
    private final CourierRepository courierRepository;

    @Autowired
    public isApprovedCourierProcess(UserRepository userRepository, CourierRepository courierRepository) {
        this.userRepository = userRepository;
        this.courierRepository = courierRepository;
    }

    @Around("@annotation(org.example.blps.annotations.isApprovedCourier)")
    public Object isApprovedCourier(ProceedingJoinPoint joinPoint) throws Throwable {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new AccessDeniedException("Пользователь не найден"));
        Courier courier = courierRepository.findByUserId(user.getId())
                .orElseThrow(() -> new AccessDeniedException("Курьер не найден"));
        if (courier.getAccountState() == CourierAccountState.BLOCKED) {
            throw new AccessDeniedException("Ваш аккаунт заблокирован");
        }
        if (courier.getAccountState() == CourierAccountState.ACTIVE) {
            return joinPoint.proceed();
        }
        throw new AccessDeniedException("Доступ к методу разрешён только для курьера со статусом ACTIVE");
    }
}
