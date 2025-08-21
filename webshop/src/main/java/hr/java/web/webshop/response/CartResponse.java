package hr.java.web.webshop.response;

import hr.java.web.webshop.dto.CartDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartResponse {
    private boolean success;
    private String message;
}
