package hr.java.web.webshop.service;


import hr.java.web.webshop.request.CheckoutRequest;
import hr.java.web.webshop.response.PayPalCaptureResponse;
import hr.java.web.webshop.response.PayPalOrderResponse;

import java.math.BigDecimal;

public interface PayPalService {
    PayPalOrderResponse createOrder(CheckoutRequest request, BigDecimal amount, String currency);
    PayPalCaptureResponse captureOrder(String orderID);
    String getAccessToken();
}