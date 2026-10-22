package cinema.security;

import cinema.dto.response.ApiErrorCode;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class JsonAccessDeniedHandler implements AccessDeniedHandler {
    private final SecurityErrorResponseWriter responseWriter;

    public JsonAccessDeniedHandler(SecurityErrorResponseWriter responseWriter) {
        this.responseWriter = responseWriter;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException exception) throws IOException {
        responseWriter.write(request, response, HttpStatus.FORBIDDEN,
                ApiErrorCode.ACCESS_DENIED, "Access to this resource is denied");
    }
}
