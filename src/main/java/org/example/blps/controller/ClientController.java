//package org.example.blps.controller;
//import org.example.blps.dto.requestDto.ClientRequestDto;
//import org.example.blps.entity.Client;
//import org.example.blps.service.ClientService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//public class ClientController  {
//
//    private final ClientService clientService;
//
//    @Autowired
//    public ClientController(ClientService clientService) {
//        this.clientService = clientService;
//    }
//
//    @PostMapping(value = "/clients")
//    public ResponseEntity<?> createClient(@RequestBody ClientRequestDto clientRequestDto) {
//        clientService.addClient(clientRequestDto);
//        return ResponseEntity.ok().build();
//    }
//}
