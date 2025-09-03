package dev.miguel.model.solicitud.proyections;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@ToString
public class FindSolicitudesDto {

        Long solicitudId;
        BigDecimal monto;
        Integer plazo;
        String correoElectronico;
        String tipoPrestamo;
        String estado;

}
