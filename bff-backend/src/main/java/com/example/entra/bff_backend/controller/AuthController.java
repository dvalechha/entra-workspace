package com.example.entra.bff_backend.controller;

import com.example.entra.bff_backend.dto.UserInfo;
import com.example.entra.bff_backend.service.AuthService;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;

    @Value("${spring.security.oauth2.client.registration.entra.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.entra.redirect-uri}")
    private String redirectUri;

    @Value("${spring.security.oauth2.client.provider.entra.issuer-uri}")
    private String issuerUri;

    @Value("${app.react-url}")
    private String reactUrl;

    @PostMapping("/session/create")
    public ResponseEntity<?> createSession(HttpSession session) {
        // Initialize session if not exists
        return ResponseEntity.ok(Map.of("sessionId", session.getId()));
    }

    @PostMapping("/session/clear")
    public ResponseEntity<?> clearSession(HttpSession session) {
        session.invalidate();

        String baseUri;
        if (issuerUri.endsWith("/v2.0")) {
             baseUri = issuerUri.substring(0, issuerUri.lastIndexOf("/v2.0"));
        } else {
             baseUri = issuerUri;
        }

        String logoutUrl = baseUri + "/oauth2/v2.0/logout" +
                "?post_logout_redirect_uri=" + URLEncoder.encode(reactUrl, StandardCharsets.UTF_8);

        return ResponseEntity.ok(Map.of("logoutUrl", logoutUrl));
    }

    @GetMapping("/session/codeUrl")
    public ResponseEntity<?> getCodeUrl(HttpSession session) throws Exception {
        // Ensure session is created
        session.getId();

        String codeVerifier = authService.generateCodeVerifier();
        String codeChallenge = authService.generateCodeChallenge(codeVerifier);

        session.setAttribute("code_verifier", codeVerifier);

        String actualRedirectUri = redirectUri.replace("{baseUrl}", "http://localhost:3001");

        // Construct auth URL: issuerUri ends with /v2.0, so we need to remove it and build the full path
        String baseUri;
        if (issuerUri.endsWith("/v2.0")) {
             baseUri = issuerUri.substring(0, issuerUri.lastIndexOf("/v2.0"));
        } else {
             baseUri = issuerUri;
        }

        String authUrl = baseUri + "/oauth2/v2.0/authorize" +
                "?client_id=" + clientId +
                "&response_type=code" +
                "&redirect_uri=" + URLEncoder.encode(actualRedirectUri, StandardCharsets.UTF_8) +
                "&response_mode=query" +
                "&scope=" + URLEncoder.encode("openid profile email offline_access api://" + clientId + "/Data.Read", StandardCharsets.UTF_8) +
                "&code_challenge=" + codeChallenge +
                "&code_challenge_method=S256";

        return ResponseEntity.ok(Map.of("url", authUrl));
    }

    @GetMapping("/session/accessToken")
    public void handleCallback(@RequestParam String code, HttpSession session, HttpServletResponse response) throws Exception {
        String codeVerifier = (String) session.getAttribute("code_verifier");
        if (codeVerifier == null) {
            response.sendError(400, "Missing code_verifier in session");
            return;
        }

        Map<String, Object> tokenResponse = authService.exchangeCodeForToken(code, codeVerifier);

        String accessToken = (String) tokenResponse.get("access_token");
        session.setAttribute("access_token", accessToken);
        session.setAttribute("refresh_token", tokenResponse.get("refresh_token"));

        // Log access token claims for debugging
        if (accessToken != null) {
            try {
                JWT accessTokenJwt = JWTParser.parse(accessToken);
                JWTClaimsSet accessTokenClaims = accessTokenJwt.getJWTClaimsSet();
                logger.info("Access Token Claims: {}", accessTokenClaims.getClaims());
                logger.info("Access Token Scopes: {}", accessTokenClaims.getStringClaim("scp"));
            } catch (Exception e) {
                logger.error("Error parsing access token", e);
            }
        }

        String idToken = (String) tokenResponse.get("id_token");
        if (idToken != null) {
            JWT jwt = JWTParser.parse(idToken);
            JWTClaimsSet claims = jwt.getJWTClaimsSet();

            // Debug logging - log all claims to see what's available
            logger.info("ID Token Claims: {}", claims.getClaims().keySet());
            logger.info("All Claims: {}", claims.getClaims());

            String userName = claims.getStringClaim("name");
            List<String> userRoles = claims.getStringListClaim("roles");

            logger.info("User Name: {}", userName);
            logger.info("Roles from 'roles' claim: {}", userRoles);

            session.setAttribute("user_name", userName);
            session.setAttribute("user_roles", userRoles != null ? userRoles : Collections.emptyList());
        }

        response.sendRedirect(reactUrl);
    }

    @GetMapping("/session/refreshToken")
    public ResponseEntity<?> refreshToken(HttpSession session) {
        String refreshToken = (String) session.getAttribute("refresh_token");
        if (refreshToken == null) {
            return ResponseEntity.status(401).body("No refresh token found");
        }

        Map<String, Object> tokenResponse = authService.refreshToken(refreshToken);
        session.setAttribute("access_token", tokenResponse.get("access_token"));
        session.setAttribute("refresh_token", tokenResponse.get("refresh_token"));

        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserInfo> getMe(HttpSession session) {
        String name = (String) session.getAttribute("user_name");
        List<String> roles = (List<String>) session.getAttribute("user_roles");

        if (name == null) {
            return ResponseEntity.status(401).build();
        }

        return ResponseEntity.ok(new UserInfo(name, roles != null ? roles : Collections.emptyList()));
    }
}
