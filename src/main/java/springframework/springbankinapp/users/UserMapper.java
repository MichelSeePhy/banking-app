package springframework.springbankinapp.users;

import org.mapstruct.*;
import springframework.springbankinapp.users.dtos.*;
import springframework.springbankinapp.users.entities.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponseDto toUserResponseDto(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "id", ignore = true)
    void update(UpdateUserDto updateUserDto, @MappingTarget User user);

    List<UserResponseDto> toUserResponseList(List<User> userList);
}
