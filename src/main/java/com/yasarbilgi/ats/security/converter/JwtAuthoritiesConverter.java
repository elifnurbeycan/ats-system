package com.yasarbilgi.ats.security.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class JwtAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    // JWT içindeki rol ve permission değerlerini Spring Security yetkilerine dönüştürür.
    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        getStringClaim(jwt, "roles").forEach(role ->
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
        getStringClaim(jwt, "permissions").forEach(permission ->
                authorities.add(new SimpleGrantedAuthority(permission)));
        return authorities;
    }

    // Çok değerli JWT claim alanını güvenli bir String listesine dönüştürür.
    private Collection<String> getStringClaim(Jwt jwt, String claimName) {
        Object claim = jwt.getClaim(claimName);
        if (!(claim instanceof Collection<?> values)) return List.of();
        return values.stream().filter(Objects::nonNull).map(Object::toString).toList();
    }
}
