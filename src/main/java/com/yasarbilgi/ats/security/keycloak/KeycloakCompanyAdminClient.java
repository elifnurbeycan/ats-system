package com.yasarbilgi.ats.security.keycloak;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yasarbilgi.ats.security.config.KeycloakAdminProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

/** Provisions a company administrator without exposing Keycloak credentials to the browser. */
@Component
@RequiredArgsConstructor
public class KeycloakCompanyAdminClient {
    private final KeycloakAdminProperties properties;

    public ProvisionedUser create(String username, String email, String firstName, String lastName,
                                  String temporaryPassword, long companyId) {
        if (!properties.enabled()) return null;
        requireConfiguration();
        RestClient client = RestClient.create(properties.serverUrl());
        String adminToken = accessToken(client);
        Map<String, Object> user = Map.of(
                "username", username, "email", email, "firstName", firstName, "lastName", lastName,
                "enabled", true, "emailVerified", false,
                "attributes", Map.of("companyId", List.of(Long.toString(companyId))),
                "credentials", List.of(Map.of("type", "password", "value", temporaryPassword, "temporary", true)));
        var response = client.post().uri("/admin/realms/{realm}/users", properties.targetRealm())
                .headers(headers -> headers.setBearerAuth(adminToken)).contentType(MediaType.APPLICATION_JSON)
                .body(user).retrieve().toBodilessEntity();
        String location = response.getHeaders().getFirst("Location");
        if (location == null || !location.contains("/users/")) throw new IllegalStateException("Keycloak kullanıcı kimliği alınamadı.");
        String userId = location.substring(location.lastIndexOf('/') + 1);
        Map<String, Object> role = ensureRole(client, adminToken);
        client.post().uri("/admin/realms/{realm}/users/{userId}/role-mappings/realm", properties.targetRealm(), userId)
                .headers(headers -> headers.setBearerAuth(adminToken)).contentType(MediaType.APPLICATION_JSON)
                .body(List.of(role)).retrieve().toBodilessEntity();
        return new ProvisionedUser(userId);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> ensureRole(RestClient client, String adminToken) {
        try {
            return client.get().uri("/admin/realms/{realm}/roles/{role}", properties.targetRealm(), properties.companyAdminRole())
                    .headers(headers -> headers.setBearerAuth(adminToken)).retrieve().body(Map.class);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() != 404) throw ex;
            client.post().uri("/admin/realms/{realm}/roles", properties.targetRealm())
                    .headers(headers -> headers.setBearerAuth(adminToken)).contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("name", properties.companyAdminRole(), "description", "ATS şirket yöneticisi"))
                    .retrieve().toBodilessEntity();
            return client.get().uri("/admin/realms/{realm}/roles/{role}", properties.targetRealm(), properties.companyAdminRole())
                    .headers(headers -> headers.setBearerAuth(adminToken)).retrieve().body(Map.class);
        }
    }

    public record ProvisionedUser(String userId) {}

    private String accessToken(RestClient client) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "client_credentials"); form.add("client_id", properties.clientId());
        form.add("client_secret", properties.clientSecret());
        TokenResponse token = client.post().uri("/realms/{realm}/protocol/openid-connect/token", properties.realm())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED).body(form).retrieve().body(TokenResponse.class);
        if (token == null || token.accessToken() == null || token.accessToken().isBlank()) throw new IllegalStateException("Keycloak Admin API erişim tokenı alınamadı.");
        return token.accessToken();
    }
    private void requireConfiguration() {
        if (properties.serverUrl().isBlank() || properties.clientId().isBlank() || properties.clientSecret().isBlank())
            throw new IllegalStateException("Keycloak Admin API ayarları eksik.");
    }
    private record TokenResponse(@JsonProperty("access_token") String accessToken) {}
}
