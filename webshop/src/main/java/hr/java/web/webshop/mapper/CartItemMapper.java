package hr.java.web.webshop.mapper;

import hr.java.web.webshop.dto.CartItemDto;
import hr.java.web.webshop.model.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CartItemMapper {
    CartItemMapper INSTANCE = Mappers.getMapper(CartItemMapper.class);

    CartItemDto toCartItemDto(CartItem cartItem);
    CartItem fromCartItemDto(CartItemDto cartItemDto);
}
