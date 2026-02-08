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

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(ProxyController.class);

    @Value("${app.data-backend-url:http://localhost:3002}")
    private String dataBackendUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/**")
    public ResponseEntity<?> proxyRequest(HttpSession session, jakarta.servlet.http.HttpServletRequest request) {
        // Correctly extract the sub-path after /v1/proxy
        String requestUri = request.getRequestURI();
        String path = requestUri.substring(requestUri.indexOf("/v1/proxy") + "/v1/proxy".length());
        
        // Prepend /v1 if it was stripped or ensure we call /v1/data/...
        String targetPath = path.startsWith("/v1") ? path : "/v1" + path;
        String url = dataBackendUrl + targetPath;

        logger.info("Proxying request: {} -> {}", requestUri, url);

        String accessToken = (String) session.getAttribute("access_token");
        logger.info("Access Token in session: {}", accessToken != null ? "Present" : "NULL");

        if (accessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No access token in session");
        }

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
            logger.error("Backend returned error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsByteArray());
        } catch (Exception e) {
            logger.error("Proxy error: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
