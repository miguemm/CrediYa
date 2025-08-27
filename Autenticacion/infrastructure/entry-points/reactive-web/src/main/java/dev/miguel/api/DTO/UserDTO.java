package dev.miguel.api.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UserDTO(
        Long id,
        String nombres,
        String apellidos,
        LocalDate fechaNacimiento,
        String direccion,
        String telefono,
        String correoElectronico,
        BigDecimal salarioBase
) {
}
