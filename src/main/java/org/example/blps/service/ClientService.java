package org.example.blps.service;
import org.example.blps.entity.Client;
import org.example.blps.entity.Courier;
import org.example.blps.entity.User;
import org.example.blps.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    @Autowired
    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public Client findByUser(User user) {
        return clientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Клиент с таким айди не найден"));
    }
}
