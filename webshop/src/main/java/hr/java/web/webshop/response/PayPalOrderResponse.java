package hr.java.web.webshop.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayPalOrderResponse {
    private String id;
    private String status;
    private String error;

    public PayPalOrderResponse(String id, String status) {
        this.id = id;
        this.status = status;
    }

    public PayPalOrderResponse(String error) {
        this.error = error;
    }

    public boolean hasError() {
        return error != null && !error.isEmpty();
    }
}