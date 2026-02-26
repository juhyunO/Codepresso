package com.codepresso.codepresso.payment.service;

import com.codepresso.codepresso.payment.dto.response.TossPaymentConfirmResponse;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TossPaymentClient {
    @Value("${toss.payments.secret-key}")
    private String secretKey;

    @Value("${toss.payments.api-url}")
    private String apiUrl;

    private final RestTemplate restTemplate;

    /**
     * Toss 결제 승인 API 호출
     */
    public TossPaymentConfirmResponse confirm(String paymentKey, String orderId, Integer amount) {
        String url = apiUrl + "/payments/confirm";

        HttpHeaders headers = createHeaders();

        Map<String, Object> body = new HashMap<>();
        body.put("paymentKey", paymentKey);
        body.put("orderId", orderId);
        body.put("amount", amount);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<TossPaymentConfirmResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    TossPaymentConfirmResponse.class
            );
            return response.getBody();
        }catch (Exception e) {
            log.error("Toss 결제 승인 실패: {}", e.getMessage());
            throw new RuntimeException("결제 승인에 실패했습니다.",e);
        }
    }

    /**
     * Authorization 헤더 생성 (Basic Auth)
     */
    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String credentials = secretKey + ":";
        String encodedCredentials = Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        headers.set("Authorization", "Basic " + encodedCredentials);

        return headers;
    }

    /**
     * paymentKey로 결제 정보 조회
     */
    public TossPaymentConfirmResponse getPaymentByPaymentKey(String paymentKey) {
        String url = apiUrl + "/payments/" + paymentKey;

        HttpHeaders headers = createHeaders();
        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<TossPaymentConfirmResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    request,
                    TossPaymentConfirmResponse.class
            );
            return response.getBody();
        } catch (Exception e) {
            log.error("Toss 결제 정보 조회 실패: {}", e.getMessage());
            return null;  // 조회 실패해도 기본 정보로 저장
        }
    }
}
