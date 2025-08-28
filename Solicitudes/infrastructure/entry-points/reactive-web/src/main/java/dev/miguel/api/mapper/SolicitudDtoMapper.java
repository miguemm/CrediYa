package dev.miguel.api.mapper;

import dev.miguel.api.DTO.CreateSolicitudDTO;
import dev.miguel.model.solicitud.Solicitud;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SolicitudDtoMapper {

    Solicitud toDomain(CreateSolicitudDTO dto);

}
