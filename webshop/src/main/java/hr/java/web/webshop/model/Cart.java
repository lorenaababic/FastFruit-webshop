package hr.java.web.webshop.model;

import lombok.Getter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;


@Getter
public class Cart implements Serializable {
    private Map<Long, CartItem> items = new HashMap<>();

    public void setItems(Map<Long, CartItem> items) {
        this.items = items != null ? items : new HashMap<>();
    }

    public boolean isEmpty() {
        return items == null || items.isEmpty();
    }

    public BigDecimal getTotalAmount() {
        return items.values().stream()
                .map(item -> item.getProduct().getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Integer getTotalQuantity() {
        return items.values().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }
}