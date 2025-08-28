package dev.miguel.api.DTO;

import java.math.BigDecimal;

public record CreateSolicitudDTO(

        BigDecimal monto,

        Integer plazo,

        String correoElectronico,

        Long estadoId,

        Long tipoPrestamoId

) {
}
