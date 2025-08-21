package hr.java.web.webshop.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemDto {
    private Long productId;
    private String productName;
    private String productImagePath;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal subtotal;
}
