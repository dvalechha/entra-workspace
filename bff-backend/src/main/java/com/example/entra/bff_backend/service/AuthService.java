package com.example.entra.bff_backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Value("${spring.security.oauth2.client.registration.entra.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.entra.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.entra.redirect-uri}")
    private String redirectUri;

    @Value("${spring.security.oauth2.client.provider.entra.issuer-uri}")
    private String issuerUri;

    private final RestTemplate restTemplate = new RestTemplate();

    public String generateCodeVerifier() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] codeVerifier = new byte[32];
        secureRandom.nextBytes(codeVerifier);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(codeVerifier);
    }

    public String generateCodeChallenge(String codeVerifier) throws NoSuchAlgorithmException {
        byte[] bytes = codeVerifier.getBytes(StandardCharsets.US_ASCII);
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        byte[] digest = messageDigest.digest(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
    }

    public Map<String, Object> exchangeCodeForToken(String code, String codeVerifier) {
        // Construct token URI: issuerUri ends with /v2.0, so we need to remove it and build the full path
        String baseUri = issuerUri.endsWith("/v2.0")
            ? issuerUri.substring(0, issuerUri.length() - 5)  // Remove /v2.0 including trailing slash
            : issuerUri;
        String tokenUri = baseUri + "/oauth2/v2.0/token";

        logger.debug("issuerUri = {}", issuerUri);
        logger.debug("baseUri = {}", baseUri);
        logger.debug("tokenUri = {}", tokenUri);

        // Correcting redirectUri placeholder if necessary (though Spring handles it, we are manual here)
        String actualRedirectUri = redirectUri.replace("{baseUrl}", "http://localhost:3001");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("redirect_uri", actualRedirectUri);
        body.add("code_verifier", codeVerifier);
        body.add("scope", "openid profile email offline_access api://" + clientId + "/Data.Read");

        return restTemplate.postForObject(tokenUri, body, Map.class);
    }

    public Map<String, Object> refreshToken(String refreshToken) {
        // Construct token URI: issuerUri ends with /v2.0, so we need to remove it and build the full path
        String baseUri = issuerUri.endsWith("/v2.0")
            ? issuerUri.substring(0, issuerUri.length() - 5)  // Remove /v2.0 including trailing slash
            : issuerUri;
        String tokenUri = baseUri + "/oauth2/v2.0/token";

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("grant_type", "refresh_token");
        body.add("refresh_token", refreshToken);
        body.add("scope", "openid profile email offline_access api://" + clientId + "/Data.Read");

        return restTemplate.postForObject(tokenUri, body, Map.class);
    }
}
