package hr.java.web.webshop.model;

import lombok.Getter;

@Getter
public enum OrderStatus {
    PENDING("Čeka obradu"),
    PROCESSING("U obradi"),
    PAID("Plaćeno"),
    SHIPPED("Poslano"),
    DELIVERED("Dostavljeno"),
    CANCELED("Otkazano");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

}
