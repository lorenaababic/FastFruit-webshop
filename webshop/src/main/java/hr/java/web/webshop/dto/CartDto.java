package hr.java.web.webshop.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class CartDto {
    private boolean success;
    private String message;
    private int itemCount;
    private BigDecimal total;
    private boolean freeShipping;
    private BigDecimal shippingCost;
    private BigDecimal finalAmount;
    private List<CartItemDto> items;
}