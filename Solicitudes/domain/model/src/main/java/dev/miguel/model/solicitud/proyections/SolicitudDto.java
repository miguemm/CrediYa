package dev.miguel.model.solicitud.proyections;

import dev.miguel.model.utils.userContext.UserDetails;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@ToString
public class SolicitudDto {

        Long usuarioId;
        Long solicitudId;
        BigDecimal monto;
        Integer plazo;
        String correoElectronico;
        String tipoPrestamo;
        String estado;
        UserDetails user;

}
