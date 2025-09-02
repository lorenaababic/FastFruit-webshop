package hr.java.web.webshop.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hr.java.web.webshop.config.PayPalConfig;
import hr.java.web.webshop.request.CheckoutRequest;
import hr.java.web.webshop.response.PayPalCaptureResponse;
import hr.java.web.webshop.response.PayPalOrderResponse;
import hr.java.web.webshop.service.PayPalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayPalServiceImpl implements PayPalService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final PayPalConfig payPalConfig;

    private String getBaseUrl() {
        return "sandbox".equals(payPalConfig.getMode())
                ? "https://api-m.sandbox.paypal.com"
                : "https://api-m.paypal.com";
    }

    @Override
    public String getAccessToken() {
        String auth = payPalConfig.getClientId() + ":" + payPalConfig.getClientSecret();
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Basic " + encodedAuth);

        HttpEntity<String> request = new HttpEntity<>("grant_type=client_credentials", headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    getBaseUrl() + "/v1/oauth2/token", request, String.class);

            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            return jsonNode.get("access_token").asText();
        } catch (Exception e) {
            log.error("Error getting PayPal access token: {}", e.getMessage());
            throw new RuntimeException("PayPal token error");
        }
    }

    @Override
    public PayPalOrderResponse createOrder(CheckoutRequest request, BigDecimal amount, String currency) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + getAccessToken());

        Map<String, Object> orderRequest = createOrderPayload(amount, currency);
        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(orderRequest, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    getBaseUrl() + "/v2/checkout/orders", httpEntity, String.class);

            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            String orderId = jsonNode.get("id").asText();
            String status = jsonNode.get("status").asText();

            log.info("PayPal order created: {}", orderId);
            return new PayPalOrderResponse(orderId, status);
        } catch (Exception e) {
            log.error("Error creating PayPal order: {}", e.getMessage());
            return new PayPalOrderResponse("Error: " + e.getMessage());
        }
    }

    @Override
    public PayPalCaptureResponse captureOrder(String orderID) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + getAccessToken());

        HttpEntity<String> httpEntity = new HttpEntity<>("{}", headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    getBaseUrl() + "/v2/checkout/orders/" + orderID + "/capture",
                    httpEntity, String.class);

            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            String id = jsonNode.get("id").asText();
            String status = jsonNode.get("status").asText();

            JsonNode payer = jsonNode.get("payer");
            String payerEmail = payer != null ? payer.path("email_address").asText() : "";

            JsonNode captureDetails = jsonNode.path("purchase_units").get(0)
                    .path("payments").path("captures").get(0);
            String amount = captureDetails.path("amount").path("value").asText();
            String currency = captureDetails.path("amount").path("currency_code").asText();

            log.info("PayPal order captured: {}", orderID);
            return new PayPalCaptureResponse(id, status, "", payerEmail, amount, currency);
        } catch (Exception e) {
            log.error("Error capturing PayPal order: {}", e.getMessage());
            return new PayPalCaptureResponse("Error: " + e.getMessage());
        }
    }

    private Map<String, Object> createOrderPayload(BigDecimal amount, String currency) {
        Map<String, Object> orderRequest = new HashMap<>();
        orderRequest.put("intent", "CAPTURE");

        Map<String, Object> amountMap = new HashMap<>();
        amountMap.put("currency_code", currency);
        amountMap.put("value", amount.setScale(2).toString());

        Map<String, Object> purchaseUnit = new HashMap<>();
        purchaseUnit.put("amount", amountMap);
        purchaseUnit.put("description", "FastFruit Online narudžba");

        orderRequest.put("purchase_units", List.of(purchaseUnit));
        return orderRequest;
    }
}