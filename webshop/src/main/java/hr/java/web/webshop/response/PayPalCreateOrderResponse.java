package hr.java.web.webshop.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class PayPalCreateOrderResponse {
    @JsonProperty("success")
        private boolean success;

        @JsonProperty("paypalOrderId")
        private String paypalOrderId;

        @JsonProperty("databaseOrderId")
        private Long databaseOrderId;

        @JsonProperty("error")
        private String error;

        private PayPalCreateOrderResponse(boolean success, String paypalOrderId, Long databaseOrderId, String error) {
            this.success = success;
            this.paypalOrderId = paypalOrderId;
            this.databaseOrderId = databaseOrderId;
            this.error = error;
        }

        public static PayPalCreateOrderResponse success(String paypalOrderId, Long databaseOrderId) {
            return new PayPalCreateOrderResponse(true, paypalOrderId, databaseOrderId, null);
        }

        public static PayPalCreateOrderResponse error(String error) {
            return new PayPalCreateOrderResponse(false, null, null, error);
        }

        public String getId() { return paypalOrderId; }
        public Long getOrderId() { return databaseOrderId; }
}
