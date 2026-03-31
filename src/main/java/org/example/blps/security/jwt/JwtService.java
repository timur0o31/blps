package org.example.blps.security.jwt;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.blps.dto.requestDto.JwtAuthificationRequestDto;
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

    // Генерация токена
    public JwtAuthificationRequestDto generateAuthToken(String email) {
        JwtAuthificationRequestDto authRequestDto = new JwtAuthificationRequestDto();
        authRequestDto.setToken(generateJwtToken(email));
        return authRequestDto;
    }

    // Рефрешь токена
    public JwtAuthificationRequestDto refreshBaseToken(String email, String refreshToken) {
        JwtAuthificationRequestDto authRequestDto = new JwtAuthificationRequestDto();
        authRequestDto.setToken(generateJwtToken(email));
        authRequestDto.setRefreshToken(refreshToken);
        return authRequestDto;
    }

    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser().verifyWith(getSignInKey()).build().parseSignedClaims(token).getPayload();
        return claims.getSubject();
    }

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

    private String generateJwtToken(String email) {
        Date date = Date.from(LocalDateTime.now().plusMinutes(20).atZone(ZoneId.systemDefault()).toInstant());
        return Jwts.builder().subject(email).expiration(date).signWith(getSignInKey()).compact();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private String generateRefreshJwtToken(String email) {
        Date date = Date.from(LocalDateTime.now().plusDays(2).atZone(ZoneId.systemDefault()).toInstant());
        return Jwts.builder().subject(email).expiration(date).signWith(getSignInKey()).compact();
    }
}
