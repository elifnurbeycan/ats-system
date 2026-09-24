package com.yasarbilgi.ats.security.converter;

import com.yasarbilgi.ats.permission.entity.PermissionCode;
import com.yasarbilgi.ats.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class JwtAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final UserRepository userRepository;

    // JWT içindeki rol ve permission değerlerini Spring Security yetkilerine dönüştürür.
    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        Set<String> roles = new HashSet<>();
        var atsUser = jwt.getSubject() == null ? Optional.<com.yasarbilgi.ats.user.entity.User>empty()
                : userRepository.findByKeycloakUserIdAndActiveTrue(jwt.getSubject());

        if (atsUser.isPresent()) {
            // Tenant kullanıcılarında rol ve permission için tek güvenilir kaynak ATS
            // veritabanıdır. Keycloak kullanıcı attribute'larından gelen claim'ler
            // yetki yükseltmek amacıyla kullanılamaz.
            var user = atsUser.get();
            user.getRoles().forEach(role -> roles.add(role.getCode()));
            user.getRoles().stream().flatMap(role -> role.getPermissions().stream())
                    .filter(permission -> permission.isActive())
                    .map(permission -> permission.getCode().name())
                    .forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission)));
        } else {
            // ATS tenant kullanıcısı olmayan hesaplarda yalnızca platform yönetici
            // rolü kabul edilir; diğer realm rolleri tenant API yetkisi sağlamaz.
            Set<String> realmRoles = new HashSet<>(getNestedStringClaim(jwt, "realm_access", "roles"));
            if (realmRoles.contains("SUPER_ADMIN")) roles.add("SUPER_ADMIN");
        }
        roles.forEach(role ->
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
        if (roles.contains("COMPANY_ADMIN")) {
            Arrays.stream(PermissionCode.values())
                    .map(Enum::name)
                    .map(SimpleGrantedAuthority::new)
                    .forEach(authorities::add);
        }
        return authorities;
    }

    private Collection<String> getNestedStringClaim(Jwt jwt, String... path) {
        Object current = jwt.getClaims();
        for (String key : path) {
            if (!(current instanceof Map<?, ?> map)) return List.of();
            current = map.get(key);
        }
        if (!(current instanceof Collection<?> values)) return List.of();
        return values.stream().filter(Objects::nonNull).map(Object::toString).toList();
    }

    // Çok değerli JWT claim alanını güvenli bir String listesine dönüştürür.
    private Collection<String> getStringClaim(Jwt jwt, String claimName) {
        Object claim = jwt.getClaim(claimName);
        if (!(claim instanceof Collection<?> values)) return List.of();
        return values.stream().filter(Objects::nonNull).map(Object::toString).toList();
    }
}
