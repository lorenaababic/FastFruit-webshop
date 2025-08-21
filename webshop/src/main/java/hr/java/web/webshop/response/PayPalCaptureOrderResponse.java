package hr.java.web.webshop.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class PayPalCaptureOrderResponse {
    @JsonProperty("success")
    private boolean success;

    @JsonProperty("databaseOrderId")
    private Long databaseOrderId;

    @JsonProperty("redirectUrl")
    private String redirectUrl;

    @JsonProperty("error")
    private String error;

    private PayPalCaptureOrderResponse(boolean success, Long databaseOrderId, String redirectUrl, String error) {
        this.success = success;
        this.databaseOrderId = databaseOrderId;
        this.redirectUrl = redirectUrl;
        this.error = error;
    }

    public static PayPalCaptureOrderResponse success(Long databaseOrderId, String redirectUrl) {
        return new PayPalCaptureOrderResponse(true, databaseOrderId, redirectUrl, null);
    }

    public static PayPalCaptureOrderResponse error(String error) {
        return new PayPalCaptureOrderResponse(false, null, null, error);
    }
}
