package hr.java.web.webshop.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CartItemDto {
    private Long productId;
    private String productName;
    private BigDecimal price;
    private int quantity;
    private BigDecimal total;
}