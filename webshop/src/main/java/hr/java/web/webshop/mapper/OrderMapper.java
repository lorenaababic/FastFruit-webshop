package hr.java.web.webshop.mapper;

import hr.java.web.webshop.dto.OrderDto;
import hr.java.web.webshop.model.Order;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface OrderMapper {
    OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

    OrderDto toOrderDto(Order order);
    Order toOrder(OrderDto orderDto);
}
