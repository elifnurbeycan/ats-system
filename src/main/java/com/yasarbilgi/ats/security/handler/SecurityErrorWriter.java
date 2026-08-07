package com.yasarbilgi.ats.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yasarbilgi.ats.common.response.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import com.yasarbilgi.ats.common.exception.ErrorCode;

@Component
@RequiredArgsConstructor
public class SecurityErrorWriter {

    private final ObjectMapper objectMapper;

    // Güvenlik hatasını uygulamanın standart JSON hata biçiminde yanıta yazar.
    public void write(HttpServletRequest request, HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ErrorCode code = status == 401 ? ErrorCode.UNAUTHORIZED : ErrorCode.FORBIDDEN;
        objectMapper.writeValue(response.getOutputStream(),
                new ApiErrorResponse(Instant.now(), status, code.name(), message,
                        request.getRequestURI(), Map.of()));
    }
}
