package hr.java.web.webshop.model;

import lombok.Getter;

@Getter
public enum PaymentMethod {
    CASH_ON_DELIVERY("Gotovina - pouzeće"), PAYPAL("PayPal");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

}
