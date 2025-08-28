package dev.miguel.api.mapper;

import dev.miguel.api.DTO.CreateUserDTO;
import dev.miguel.model.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserDtoMapper {

    @Mapping(target = "id", ignore = true)
    User toDomain(CreateUserDTO dto);

}
