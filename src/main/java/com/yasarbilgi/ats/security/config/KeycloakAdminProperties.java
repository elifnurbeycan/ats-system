package com.yasarbilgi.ats.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.keycloak.admin")
public record KeycloakAdminProperties(
        boolean enabled, String serverUrl, String realm, String clientId,
        String clientSecret, String targetRealm, String companyAdminRole) {
    public KeycloakAdminProperties {
        serverUrl = serverUrl == null ? "" : serverUrl.replaceAll("/+$", "");
        realm = realm == null || realm.isBlank() ? "master" : realm;
        targetRealm = targetRealm == null || targetRealm.isBlank() ? "ats" : targetRealm;
        companyAdminRole = companyAdminRole == null || companyAdminRole.isBlank()
                ? "COMPANY_ADMIN" : companyAdminRole;
    }
}
