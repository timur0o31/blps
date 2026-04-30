package org.example.blps.config;
import lombok.RequiredArgsConstructor;
import org.example.blps.entity.Admin;
import org.example.blps.entity.Courier;
import org.example.blps.entity.User;
import org.example.blps.enums.CourierAccountState;
import org.example.blps.enums.Role;
import org.example.blps.repository.AdminRepository;
import org.example.blps.repository.CourierRepository;
import org.example.blps.repository.UserRepository;
import org.example.blps.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("accessSecurity")
@RequiredArgsConstructor
public class AccessSecurity {

    private final CourierRepository courierRepository;
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;

    private User getUser(Authentication authentication){
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CustomUserDetails userDetails)) {
            return null;
        }
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }
    public boolean isApprovedCourier(Authentication authentication) {
        User user = getUser(authentication);
        if (user==null || user.getRole() != Role.COURIER) {
            return false;
        }
        Courier courier = courierRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Курьер не найден"));

        if (courier.getAccountState() == CourierAccountState.BLOCKED) {
            throw new RuntimeException("Ваш аккаунт заблокирован");
        }
        return courier.getAccountState() == CourierAccountState.ACTIVE;
    }
    public boolean isSuperUser(Authentication authentication) {
        User user = getUser(authentication);
        return user != null
                && user.getRole() == Role.ADMIN
                && user.isSuperUser();
    }
    public boolean isApprovedAdmin(Authentication authentication) {
        User user = getUser(authentication);
        if (user == null || user.getRole() != Role.ADMIN) {
            return false;
        }
        return adminRepository.findByUserId(user.getId())
                .map(Admin::isAccountState)
                .orElseThrow(()->new RuntimeException("Ваш аккаунт еще не одобрен"));
    }

}