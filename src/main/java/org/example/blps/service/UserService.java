package org.example.blps.service;
import org.example.blps.dto.requestDto.JwtAuthificationRequestDto;
import org.example.blps.dto.requestDto.JwtRefreshRequestDto;
import org.example.blps.dto.requestDto.UserCredetionalDto;
import org.example.blps.dto.requestDto.UserRequestDto;
import org.example.blps.entity.User;
import org.example.blps.mapper.UserMapper;
import org.example.blps.repository.UserRepository;
import org.example.blps.security.jwt.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import javax.naming.AuthenticationException;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, UserMapper userMapper, JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public JwtAuthificationRequestDto signIn(UserCredetionalDto userCredetionalDto) {
        User user = findByCredetionals(userCredetionalDto);
        return jwtService.generateAuthToken(user.getEmail());
    }

    public  JwtAuthificationRequestDto refreshToken(JwtRefreshRequestDto token) {
        String refreshToken = token.getRefreshToken();
        if (refreshToken == null && jwtService.validateJwtToken(refreshToken)) {
            User user = findByEmail(jwtService.getEmailFromToken(refreshToken));
            return jwtService.refreshBaseToken(user.getEmail(), refreshToken);
        }
        return null;
    }

    public void createUser(UserRequestDto userRequestDto) {
        User user = userMapper.fromDtoToEntity(userRequestDto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

    }

    private User findByCredetionals(UserCredetionalDto userCredetionalDto)  {
        Optional<User> userOptional = userRepository.findByEmail(userCredetionalDto.getEmail());
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (passwordEncoder.matches(userCredetionalDto.getPassword(), user.getPassword())) {
                return user;
            }
        }
        return null;
//        throw new AuthenticationException("password incorrect!");
    }

    private User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
}
