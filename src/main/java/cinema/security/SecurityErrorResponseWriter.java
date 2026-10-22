package cinema.security;

import cinema.dto.response.ApiErrorCode;
import cinema.dto.response.ApiErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

@Component
public class SecurityErrorResponseWriter {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void write(HttpServletRequest request, HttpServletResponse response,
                      HttpStatus status, ApiErrorCode code, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        ApiErrorResponse body = new ApiErrorResponse(
                status.value(), code, message, request.getRequestURI(), Map.of());
        objectMapper.writeValue(response.getWriter(), body);
    }
}
