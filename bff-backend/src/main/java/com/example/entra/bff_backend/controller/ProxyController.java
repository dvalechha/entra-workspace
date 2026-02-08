package com.example.entra.bff_backend.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@RestController
@RequestMapping("/v1/proxy")
@RequiredArgsConstructor
public class ProxyController {

    @Value("${app.data-backend-url:http://localhost:3002}")
    private String dataBackendUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/**")
    public ResponseEntity<?> proxyRequest(HttpSession session, jakarta.servlet.http.HttpServletRequest request) {
        String accessToken = (String) session.getAttribute("access_token");
        if (accessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No access token in session");
        }

        String path = request.getRequestURI().replace("/v1/proxy", "");
        String url = dataBackendUrl + path;
        if (request.getQueryString() != null) {
            url += "?" + request.getQueryString();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            return restTemplate.exchange(url, HttpMethod.GET, entity, byte[].class);
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsByteArray());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
