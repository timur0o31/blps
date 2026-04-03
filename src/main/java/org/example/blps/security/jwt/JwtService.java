package org.example.blps.security.jwt;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.blps.dto.responseDto.JwtAuthificationResponceDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
public class JwtService {

    private static Logger LOGGER = LogManager.getLogger(JwtService.class);

    @Value("N8BpLCtew37WlY7DgaFpJueGZhv8CfAhy6BEI2PNcT0")
    private String jwtSecret;

    // Генерация токена для пользователя с ответом
    public JwtAuthificationResponceDto generateAuthToken(String email) {
        JwtAuthificationResponceDto jwtAuthificationResponceDto = new JwtAuthificationResponceDto();
        jwtAuthificationResponceDto.setToken(generateJwtToken(email));
        return jwtAuthificationResponceDto;
    }

    // Генерация токена
    private String generateJwtToken(String email) {
        Date date = Date.from(LocalDateTime.now().plusMinutes(20).atZone(ZoneId.systemDefault()).toInstant());
        return Jwts.builder()
                .subject(email).  // токен для кого
                expiration(date)  // соклько времени будет жить
                .signWith(getSignInKey())  // SignWith - это подпись токена.
                // Подпись (из Google) - это третья часть токена создаваемая криптографическим хешированием
                .compact();
    }

    // Генерация подписи - необходима для проверки, что токен не был подделан
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Вот тут как раз проверка, что токен валидный и мы достаем email, потому что для него генерировали токен, так же можно достать время истечения токена
    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser().verifyWith(getSignInKey()).build().parseSignedClaims(token).getPayload();
        return claims.getSubject();
    }

    // наша валидация
    public boolean validateJwtToken(String token) {
        try {
            Jwts.parser().verifyWith(getSignInKey()).build().parseClaimsJws(token).getPayload();
            return true;
        } catch (ExpiredJwtException e) {
            LOGGER.error("Expired JWT token");
        } catch (UnsupportedJwtException e) {
            LOGGER.error("Unsupported JWT token");
        } catch (MalformedJwtException e) {
            LOGGER.error("Malformed JWT token");
        } catch (SecurityException e) {
            LOGGER.error("Security exception");
        } catch (Exception e) {
            LOGGER.error("Exception");
        }
        return false;
    }

}
