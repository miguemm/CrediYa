package dev.miguel.api.mapper;

import dev.miguel.api.DTO.CreateSolicitudDTO;
import dev.miguel.model.solicitud.Solicitud;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SolicitudDtoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "estadoId", ignore = true)
    @Mapping(target = "usuarioId", ignore = true)
    Solicitud toDomain(CreateSolicitudDTO dto);

}
