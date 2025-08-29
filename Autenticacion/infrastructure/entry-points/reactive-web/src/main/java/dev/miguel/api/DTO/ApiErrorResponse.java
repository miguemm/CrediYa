package dev.miguel.api.DTO;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record ApiErrorResponse(

        @Schema(
                description = "Lista con los mensajes de error",
                example = "{\"correo invalido\", \"nombre invalido\", \"usuario ya existe\"}"
        )
        List<String> errores
) {
}
