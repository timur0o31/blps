package org.example.blps.service;
import org.example.blps.dto.responseDto.JwtAuthificationResponceDto;
import org.example.blps.dto.requestDto.UserCredentialsRequestDto;
import org.example.blps.dto.requestDto.UserRequestDto;
import org.example.blps.entity.Client;
import org.example.blps.entity.Courier;
import org.example.blps.entity.User;
import org.example.blps.enums.CourierStatus;
import org.example.blps.enums.Role;
import org.example.blps.mapper.UserMapper;
import org.example.blps.repository.ClientRepository;
import org.example.blps.repository.CourierRepository;
import org.example.blps.repository.UserRepository;
import org.example.blps.security.jwt.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final CourierRepository courierRepository;
    private final ClientRepository clientRepository;

    @Autowired
    public UserService(UserRepository userRepository, UserMapper userMapper, JwtService jwtService,
                       PasswordEncoder passwordEncoder, ClientRepository clientRepository, CourierRepository courierRepository) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.courierRepository = courierRepository;
        this.clientRepository = clientRepository;
    }


    // Аутинфикация
    public JwtAuthificationResponceDto signIn(UserCredentialsRequestDto userCredetionalDto) {
        User user = findByCredetionals(userCredetionalDto);
        if (user == null) {
            throw new IllegalArgumentException("Неверный логин или пароль");
        }
        return jwtService.generateAuthToken(user.getEmail());
    }

    public User createUser(UserRequestDto userRequestDto) {
        User user = userMapper.fromDtoToEntity(userRequestDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return user;
    }

    public void createClient(UserRequestDto userRequestDto) {
       User user = createUser(userRequestDto);
        user.setRole(Role.CLIENT);
        userRepository.save(user);
        Client client = new Client();
        client.setUser(user);
        clientRepository.save(client);
    }

    public void createCourier(UserRequestDto userRequestDto) {
        User user = createUser(userRequestDto);
        user.setRole(Role.COURIER);
        userRepository.save(user);
        Courier courier = new Courier();
        courier.setUser(user);
        courier.setStatus(CourierStatus.ONLINE);
        courierRepository.save(courier);
    }

    private User findByCredetionals(UserCredentialsRequestDto userCredetionalDto)  {
        Optional<User> userOptional = userRepository.findByEmail(userCredetionalDto.getEmail());
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (passwordEncoder.matches(userCredetionalDto.getPassword(), user.getPassword())) {
                return user;
            }
        }
        return null;
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
