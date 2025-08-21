package hr.java.web.webshop.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import hr.java.web.webshop.config.PayPalConfig;
import hr.java.web.webshop.request.CheckoutRequest;
import hr.java.web.webshop.response.PayPalCaptureResponse;
import hr.java.web.webshop.response.PayPalOrderResponse;
import hr.java.web.webshop.service.PayPalService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PayPalServiceImpl implements PayPalService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final PayPalConfig payPalConfig;

    private static final Logger logger = LoggerFactory.getLogger(PayPalServiceImpl.class);

    private String getBaseUrl() {
        return "sandbox".equals(payPalConfig.getMode())
                ? "https://api-m.sandbox.paypal.com"
                : "https://api-m.paypal.com";
    }

    @Override
    public String getAccessToken() {
        try {
            String auth = payPalConfig.getClientId() + ":" + payPalConfig.getClientSecret();
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", "Basic " + encodedAuth);

            String body = "grant_type=client_credentials";
            HttpEntity<String> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    getBaseUrl() + "/v1/oauth2/token",
                    request,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                String token = jsonNode.get("access_token").asText();
                logger.info("PayPal access token acquired successfully");
                return token;
            } else {
                logger.error("Failed to get PayPal access token. Status: {}", response.getStatusCode());
                throw new RuntimeException("Failed to get PayPal access token");
            }

        } catch (Exception e) {
            logger.error("Error getting PayPal access token", e);
            throw new RuntimeException("Error getting PayPal access token: " + e.getMessage());
        }
    }

    @Override
    public PayPalOrderResponse createOrder(CheckoutRequest request, BigDecimal amount, String currency) {
        try {
            String accessToken = getAccessToken();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("PayPal-Request-Id", java.util.UUID.randomUUID().toString());

            Map<String, Object> orderRequest = createOrderPayload(amount, currency, request);

            HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(orderRequest, headers);

            logger.info("Creating PayPal order with payload: {}", orderRequest);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    getBaseUrl() + "/v2/checkout/orders",
                    httpEntity,
                    String.class
            );

            logger.info("PayPal API Response Status: {}", response.getStatusCode());
            logger.info("PayPal API Response Body: {}", response.getBody());

            if (response.getStatusCode() == HttpStatus.CREATED) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                String orderId = jsonNode.get("id").asText();
                String status = jsonNode.get("status").asText();

                logger.info("PayPal order created successfully. Order ID: {}, Status: {}", orderId, status);
                return new PayPalOrderResponse(orderId, status);
            } else {
                logger.error("Failed to create PayPal order. Status: {}, Response: {}",
                        response.getStatusCode(), response.getBody());
                return new PayPalOrderResponse("Failed to create PayPal order: " + response.getBody());
            }

        } catch (Exception e) {
            logger.error("Error creating PayPal order", e);
            return new PayPalOrderResponse("Error creating PayPal order: " + e.getMessage());
        }
    }

    @Override
    public PayPalCaptureResponse captureOrder(String orderID) {
        try {
            String accessToken = getAccessToken();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + accessToken);

            HttpEntity<String> httpEntity = new HttpEntity<>("{}", headers);

            logger.info("Capturing PayPal order: {}", orderID);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    getBaseUrl() + "/v2/checkout/orders/" + orderID + "/capture",
                    httpEntity,
                    String.class
            );

            logger.info("PayPal Capture Response Status: {}", response.getStatusCode());
            logger.info("PayPal Capture Response Body: {}", response.getBody());

            if (response.getStatusCode() == HttpStatus.CREATED) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());

                String id = jsonNode.get("id").asText();
                String status = jsonNode.get("status").asText();

                JsonNode payer = jsonNode.get("payer");
                String payerId = payer != null && payer.has("payer_id") ? payer.get("payer_id").asText() : "";
                String payerEmail = "";
                if (payer != null && payer.has("email_address")) {
                    payerEmail = payer.get("email_address").asText();
                }

                JsonNode captureDetails = jsonNode.path("purchase_units").get(0).path("payments").path("captures").get(0);
                String amount = captureDetails.path("amount").path("value").asText();
                String currency = captureDetails.path("amount").path("currency_code").asText();

                logger.info("PayPal order captured successfully. Order ID: {}", orderID);
                return new PayPalCaptureResponse(id, status, payerId, payerEmail, amount, currency);

            } else {
                logger.error("Failed to capture PayPal order. Status: {}, Response: {}",
                        response.getStatusCode(), response.getBody());
                return new PayPalCaptureResponse("Failed to capture PayPal order: " + response.getBody());
            }

        } catch (Exception e) {
            logger.error("Error capturing PayPal order: {}", orderID, e);
            return new PayPalCaptureResponse("Error capturing PayPal order: " + e.getMessage());
        }
    }

    private Map<String, Object> createOrderPayload(BigDecimal amount, String currency, CheckoutRequest request) {
        Map<String, Object> orderRequest = new HashMap<>();
        orderRequest.put("intent", "CAPTURE");

        Map<String, Object> purchaseUnit = new HashMap<>();

        Map<String, Object> amountMap = new HashMap<>();
        amountMap.put("currency_code", currency);
        amountMap.put("value", amount.setScale(2).toString());

        purchaseUnit.put("amount", amountMap);
        purchaseUnit.put("description", "FastFruit Online narudžba");
        purchaseUnit.put("custom_id", "ORDER_" + System.currentTimeMillis());

        orderRequest.put("purchase_units", List.of(purchaseUnit));



        return orderRequest;
    }
}