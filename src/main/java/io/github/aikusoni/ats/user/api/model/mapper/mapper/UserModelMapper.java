package io.github.aikusoni.ats.user.api.model.mapper.mapper;

import io.github.aikusoni.ats.user.api.model.dto.UserDto;
import io.github.aikusoni.ats.user.api.model.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserModelMapper {
    UserDto toUserDto(UserEntity userEntity);

    List<UserDto> toUserDtoList(List<UserEntity> userEntityList);

    UserEntity toUserEntity(UserDto userDto);
}
