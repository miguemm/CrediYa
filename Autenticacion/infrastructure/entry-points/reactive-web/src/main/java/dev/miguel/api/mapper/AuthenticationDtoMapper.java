package dev.miguel.api.mapper;

import dev.miguel.api.DTO.TokenDTO;
import dev.miguel.model.user.Token;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthenticationDtoMapper {

    Token toDomain(TokenDTO dto);

}
