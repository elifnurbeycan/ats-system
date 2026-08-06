package com.yasarbilgi.ats.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yasarbilgi.ats.common.exception.ApiErrorResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SecurityErrorWriter {

    private final ObjectMapper objectMapper;

    // Güvenlik hatasını uygulamanın standart JSON hata biçiminde yanıta yazar.
    public void write(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(),
                new ApiErrorResponse(Instant.now(), status, message, Map.of()));
    }
}
