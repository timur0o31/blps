package org.example.blps.service;

import org.example.blps.annotations.isApprovedAdmin;
import org.example.blps.annotations.isSuperUser;
import org.example.blps.dto.requestDto.UserRequestDto;
import org.example.blps.dto.responseDto.ResponsePaginationDto;
import org.example.blps.entity.Admin;
import org.example.blps.entity.User;
import org.example.blps.enums.Role;
import org.example.blps.repository.AdminRepository;
import org.example.blps.utils.PaginationUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@Service
public class AdminService {
    private final AdminRepository adminRepository;
    private final UserService userService;

    public AdminService(AdminRepository adminRepository, UserService userService){
        this.adminRepository = adminRepository;
        this.userService = userService;
    }
    public Admin findByUserId(Long id){
        return adminRepository.findByUserId(id).orElseThrow(()-> new IllegalStateException("Пользователя с данным id не существует"));
    }

    @Transactional
    public void changeState(String email, Long id, boolean state){
        User changedBy = userService.findByEmail(email);
        if (changedBy.getRole() != Role.ADMIN || !changedBy.isSuperUser()) {
            throw new AccessDeniedException("Включать и выключать администраторов может только суперпользователь");
        }
        Admin admin = adminRepository.findById(id)
                .orElseThrow(()->new IllegalStateException("Админа с таким id не существует"));
        User targetUser = userService.findById(admin.getUserId());
        if (targetUser.isSuperUser()) {
            throw new IllegalStateException("Нельзя изменить состояние аккаунта суперпользователя");
        }
        admin.setAccountState(state);
        adminRepository.save(admin);
    }

    @isApprovedAdmin
    public void createAdmin(UserRequestDto userRequestDto) throws IOException {
        userService.createAdmin(userRequestDto);
    }

    @isSuperUser
    public ResponsePaginationDto<Admin> getAll(String page, String size) {
        PaginationUtil.Params params = PaginationUtil.parse(page, size);
        Pageable pageable = PageRequest.of((int) params.page(), (int) params.size(), Sort.by("id").ascending());
        Page<Admin> admins = adminRepository.findAll(pageable);
        return PaginationUtil.responsePaginationDto(admins.getContent(), params, admins.getTotalElements());
    }

}
