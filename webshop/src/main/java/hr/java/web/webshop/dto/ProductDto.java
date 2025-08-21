package hr.java.web.webshop.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductDto {
    private String name;
    private String description;
    private BigDecimal price;
    private String imagePath;
    private Integer quantity;

    private Long categoryId;
}
