package hr.java.web.webshop.mapper;

import hr.java.web.webshop.dto.UserRegistrationDto;
import hr.java.web.webshop.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    UserRegistrationDto toUserRegistrationDto(User user);
    User toUser(UserRegistrationDto registrationDto);
}
