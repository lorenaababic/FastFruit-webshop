package hr.java.web.webshop.mapper;

import hr.java.web.webshop.dto.CartDto;
import hr.java.web.webshop.dto.CartItemDto;
import hr.java.web.webshop.model.Cart;
import hr.java.web.webshop.model.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mapper(uses = CartItemMapper.class)
public abstract class CartMapper {

    public static final CartMapper INSTANCE = Mappers.getMapper(CartMapper.class);

    public abstract Cart toCart(CartDto cartDto);

    public CartDto toCartDto(Cart cart) {
        if (cart == null) {
            return null;
        }

        CartDto dto = new CartDto();
        if (cart.getItems() != null) {
            List<CartItemDto> itemDtos = new ArrayList<>();
            for (CartItem item : cart.getItems().values()) {
                itemDtos.add(CartItemMapper.INSTANCE.toCartItemDto(item));
            }
            dto.setItems(itemDtos);
            dto.setItemCount(itemDtos.size());
        }

        dto.setTotal(cart.getTotalAmount());
        return dto;
    }

    protected Map<Long, CartItem> map(List<CartItemDto> items) {
        if (items == null) {
            return null;
        }
        Map<Long, CartItem> map = new java.util.HashMap<>();
        for (CartItemDto dto : items) {
            CartItem item = CartItemMapper.INSTANCE.fromCartItemDto(dto);
            map.put(dto.getProductId(), item);
        }
        return map;
    }

}

