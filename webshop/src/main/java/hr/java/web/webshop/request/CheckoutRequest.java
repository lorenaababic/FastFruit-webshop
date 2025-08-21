package hr.java.web.webshop.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequest {
    private String name;
    private String email;
    private String shippingAddress;
    private String paymentMethod;
}

