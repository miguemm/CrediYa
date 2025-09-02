package dev.miguel.security.jwt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;


@Configuration
public class JwtProvider {

    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.secret-is-base64}")
    private boolean secretIsB64;

    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        // Usa exactamente el mismo formato que al firmar: texto plano vs Base64
        byte[] keyBytes = secretIsB64
                ? java.util.Base64.getDecoder().decode(secret)
                : secret.getBytes(java.nio.charset.StandardCharsets.UTF_8);

        // HS384 ⇒ HmacSHA384 y algoritmo HS384
        var key = new javax.crypto.spec.SecretKeySpec(keyBytes, "HmacSHA384");

        var decoder = org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder
                .withSecretKey(key)
                .macAlgorithm(org.springframework.security.oauth2.jose.jws.MacAlgorithm.HS384)
                .build();

        // Validaciones por defecto (exp/nbf) sin exigir issuer
        var defaults = org.springframework.security.oauth2.jwt.JwtValidators.createDefault();
        var skew = new org.springframework.security.oauth2.jwt.JwtTimestampValidator(java.time.Duration.ofMinutes(5));
        decoder.setJwtValidator(new org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator<>(defaults, skew));

        return decoder;
    }

}
