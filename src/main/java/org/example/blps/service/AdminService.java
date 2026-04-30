package org.example.blps.service;

import org.example.blps.dto.requestDto.UserRequestDto;
import org.example.blps.entity.Admin;
import org.example.blps.entity.User;
import org.example.blps.repository.AdminRepository;
import org.springframework.stereotype.Service;

import javax.imageio.IIOException;
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
        return adminRepository.findByUserId(id).orElseThrow(()-> new RuntimeException("Пользователя с данным id не существует"));
    }
    public void changeState(String email,Long id, boolean state){
        User user = userService.findByEmail(email);
        Admin admin = adminRepository.findById(id).orElseThrow(
                ()->new RuntimeException("Админа с таким id не существует")
        );
        Admin changedBy = adminRepository.findByUserId(user.getId()).orElseThrow(()->new RuntimeException("Данный пользователь не является админом"));
        if (!changedBy.isAccountState()){
            throw new RuntimeException("Ваш аккаунт не одобрен, у вас нет привилегий администратора");
        }
        if (changedBy.getUserId()==id){
            throw new RuntimeException("Вы не можете менять свои привилегии");
        }
        admin.setAccountState(true);
        adminRepository.save(admin);
    }
    public void createAdmin(UserRequestDto userRequestDto) throws IOException {
        userService.createAdmin(userRequestDto);
    }
}
