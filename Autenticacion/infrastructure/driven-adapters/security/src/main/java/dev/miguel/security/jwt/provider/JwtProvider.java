package dev.miguel.security.jwt.provider;

import dev.miguel.model.rol.gateways.RolRepository;
import dev.miguel.model.user.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Component
@Log4j2
public class JwtProvider {

    private RolRepository rolRepository;

    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration}")
    private Integer expiration;

    public JwtProvider(RolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    public Mono<String> generateToken(User user) {
        final Instant now = Instant.now();
        final Instant exp = now.plusSeconds(expiration);

        return rolRepository.findRolById(user.getRolId())
                .map(rol -> Jwts.builder()
                        .subject(String.valueOf(user.getId()))
                        .claim("email", user.getCorreoElectronico())
                        .claim("roles", List.of(rol.getNombre()))
                        .issuedAt(Date.from(now))
                        .expiration(Date.from(exp))
                        .signWith(getKey(secret))
                        .compact());
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey(secret))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getSubject(String token) {
        return Jwts.parser()
                .verifyWith(getKey(secret))
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validate(String token){
        try {
            Jwts.parser()
                    .verifyWith(getKey(secret))
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();
            return true;
        } catch (ExpiredJwtException e) {
            log.info("token expired");
        } catch (UnsupportedJwtException e) {
            log.info("token unsupported");
        } catch (MalformedJwtException e) {
            log.info("token malformed");
        } catch (SignatureException e) {
            log.info("bad signature");
        } catch (IllegalArgumentException e) {
            log.info("illegal args");
        }
        return false;
    }

    private SecretKey getKey(String secret) {
        byte[] secretBytes = Decoders.BASE64URL.decode(secret);
        return Keys.hmacShaKeyFor(secretBytes);
    }
}