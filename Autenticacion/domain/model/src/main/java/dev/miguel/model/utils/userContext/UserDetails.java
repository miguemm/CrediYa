package dev.miguel.model.utils.userContext;

import lombok.*;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
@ToString
public class UserDetails {
    Long id;
    String nombres;
    String apellidos;
    String correoElectronico;
    BigDecimal salarioBase;
}
