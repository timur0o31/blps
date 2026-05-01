package org.example.blps.annotations;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.example.blps.entity.User;
import org.example.blps.enums.Role;
import org.example.blps.repository.AdminRepository;
import org.example.blps.repository.UserRepository;
import org.example.blps.security.CustomUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;


@Aspect
@Component
public class isSuperUserProcess {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;

    @Autowired
    public isSuperUserProcess(UserRepository userRepository, AdminRepository adminRepository) {
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
    }

    @Around("@annotation(isSuperUser)")
    public Object isApprovedCourier(ProceedingJoinPoint joinPoint) throws Throwable {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new AccessDeniedException("Пользователь не найден"));
        if ( user != null
                && user.getRole() == Role.ADMIN
                && user.isSuperUser()) {
            joinPoint.proceed();
        }
        throw new AccessDeniedException("Доступ к методу разрешён только для суперпользователя");

    }

}
