package dev.miguel.api.mapper;

import dev.miguel.api.DTO.CreateUserDTO;
import dev.miguel.model.user.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserDtoMapper {

    User toDomain(CreateUserDTO dto);

}
