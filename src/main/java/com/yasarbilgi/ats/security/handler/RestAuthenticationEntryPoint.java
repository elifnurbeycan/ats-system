package com.yasarbilgi.ats.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final SecurityErrorWriter errorWriter;

    // Kimliği doğrulanmamış API isteğine standart 401 yanıtı döner.
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException exception) throws IOException, ServletException {
        errorWriter.write(response, HttpStatus.UNAUTHORIZED.value(),
                "Bu işlem için oturum açmanız gerekiyor.");
    }
}
