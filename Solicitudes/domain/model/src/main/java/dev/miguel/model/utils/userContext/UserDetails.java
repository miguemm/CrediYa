package dev.miguel.model.utils.userContext;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UserDetails(

        Long id,
        String nombres,
        String apellidos,
        LocalDate fechaNacimiento,
        String direccion,
        String telefono,
        String correoElectronico,
        String contrasenia,
        BigDecimal salarioBase,
        Long rolId

)
{}
