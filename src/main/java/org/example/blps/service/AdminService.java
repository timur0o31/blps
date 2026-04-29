package org.example.blps.service;

import org.example.blps.entity.Admin;
import org.example.blps.repository.AdminRepository;
import org.springframework.stereotype.Service;

@Service
public class AdminService {
    private final AdminRepository adminRepository;
    public AdminService(AdminRepository adminRepository){
        this.adminRepository = adminRepository;
    }
    public Admin findByUserId(Long id){
        return adminRepository.findByUserId(id).orElseThrow(()-> new RuntimeException("Пользователя с данным id не существует"));
    }
}
