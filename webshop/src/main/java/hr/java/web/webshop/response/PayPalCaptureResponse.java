package hr.java.web.webshop.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayPalCaptureResponse {
    private String id;
    private String status;
    private String payerId;
    private String payerEmail;
    private String amount;
    private String currency;
    private String error;

    public PayPalCaptureResponse(String id, String status, String payerId, String payerEmail, String amount, String currency) {
        this.id = id;
        this.status = status;
        this.payerId = payerId;
        this.payerEmail = payerEmail;
        this.amount = amount;
        this.currency = currency;
    }

    public PayPalCaptureResponse(String error) {
        this.error = error;
    }

    public boolean hasError() {
        return error != null && !error.isEmpty();
    }
}