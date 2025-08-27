package dev.miguel.api.mapper;

import dev.miguel.api.DTO.CreateUserDTO;
import dev.miguel.api.DTO.UserDTO;
import dev.miguel.model.usuario.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserDtoMapper {

    User toDomain(CreateUserDTO dto);

    UserDTO toDto(User user);

}
