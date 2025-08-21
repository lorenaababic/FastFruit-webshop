package hr.java.web.webshop.mapper;

import hr.java.web.webshop.dto.LoginLogDto;
import hr.java.web.webshop.model.LoginLog;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface LoginLogMapper {
    LoginLogMapper INSTANCE = Mappers.getMapper(LoginLogMapper.class);

    LoginLogDto toDto(LoginLog loginLog);
    LoginLog toEntity(LoginLogDto loginLogDto);
}
