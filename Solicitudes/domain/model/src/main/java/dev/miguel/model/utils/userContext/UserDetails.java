package dev.miguel.model.utils.userContext;

import java.math.BigDecimal;

public record UserDetails(

        Long id,
        String nombres,
        String apellidos,
        String correoElectronico,
        BigDecimal salarioBase

)
{}
