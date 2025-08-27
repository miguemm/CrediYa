package dev.miguel.api.DTO;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateUserDTO(
        String nombres,
        String apellidos,
        LocalDate fechaNacimiento,
        String direccion,
        String telefono,
        String correoElectronico,
        BigDecimal salario
) {
}
