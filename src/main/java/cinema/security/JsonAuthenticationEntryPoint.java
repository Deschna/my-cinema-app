package cinema.security;

import cinema.dto.response.ApiErrorCode;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private static final Log LOGGER = LogFactory.getLog(JsonAuthenticationEntryPoint.class);
    private final SecurityErrorResponseWriter responseWriter;

    public JsonAuthenticationEntryPoint(SecurityErrorResponseWriter responseWriter) {
        this.responseWriter = responseWriter;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException exception) throws IOException {
        if (exception instanceof AuthenticationServiceException) {
            LOGGER.error("Authentication service failure for request " + request.getRequestURI(),
                    exception);
            responseWriter.write(request, response, HttpStatus.INTERNAL_SERVER_ERROR,
                    ApiErrorCode.INTERNAL_ERROR, "An unexpected error occurred");
            return;
        }
        // Tell the client to authenticate using HTTP Basic.
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"Realm\"");
        responseWriter.write(request, response, HttpStatus.UNAUTHORIZED,
                ApiErrorCode.AUTHENTICATION_REQUIRED, "Authentication is required");
    }
}
