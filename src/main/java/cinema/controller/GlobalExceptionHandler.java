package cinema.controller;

import cinema.dto.response.ApiErrorCode;
import cinema.dto.response.ApiErrorResponse;
import cinema.exception.ResourceNotFoundException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ElementKind;
import javax.validation.Path;
import org.springframework.beans.TypeMismatchException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.NonNull;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final String VALIDATION_ERROR_MESSAGE = "Request validation failed";
    private static final String INVALID_PARAMETER_MESSAGE = "Request parameters are invalid";
    private static final String INTERNAL_ERROR_MESSAGE = "An unexpected error occurred";
    private static final String GLOBAL_ERROR_KEY = "request";

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        Map<String, List<String>> fieldErrors = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(violation -> addFieldError(
                fieldErrors,
                fieldName(violation),
                violation.getMessage()
        ));
        ApiErrorResponse body = createErrorResponse(
                status,
                ApiErrorCode.VALIDATION_ERROR,
                VALIDATION_ERROR_MESSAGE,
                request,
                fieldErrors
        );
        return new ResponseEntity<>(body, status);
    }

    @Override
    @NonNull
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            @NonNull MethodArgumentNotValidException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatus status,
            @NonNull WebRequest request) {
        Map<String, List<String>> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> addFieldError(
                fieldErrors,
                error.getField(),
                defaultMessage(error)
        ));
        ex.getBindingResult().getGlobalErrors().forEach(error -> addFieldError(
                fieldErrors,
                GLOBAL_ERROR_KEY,
                defaultMessage(error)
        ));
        ApiErrorResponse body = createErrorResponse(
                status,
                ApiErrorCode.VALIDATION_ERROR,
                VALIDATION_ERROR_MESSAGE,
                request,
                fieldErrors
        );
        return new ResponseEntity<>(body, headers, status);
    }

    @Override
    @NonNull
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            @NonNull HttpMessageNotReadableException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatus status,
            @NonNull WebRequest request) {
        ApiErrorResponse body = createErrorResponse(
                status,
                ApiErrorCode.MALFORMED_REQUEST_BODY,
                "Request body is malformed or unreadable",
                request,
                Map.of()
        );
        return new ResponseEntity<>(body, headers, status);
    }

    @Override
    @NonNull
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            @NonNull MissingServletRequestParameterException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatus status,
            @NonNull WebRequest request) {
        Map<String, List<String>> fieldErrors = Map.of(
                ex.getParameterName(),
                List.of("Required request parameter is missing")
        );
        ApiErrorResponse body = createErrorResponse(
                status,
                ApiErrorCode.INVALID_REQUEST_PARAMETER,
                INVALID_PARAMETER_MESSAGE,
                request,
                fieldErrors
        );
        return new ResponseEntity<>(body, headers, status);
    }

    @Override
    @NonNull
    protected ResponseEntity<Object> handleTypeMismatch(
            @NonNull TypeMismatchException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatus status,
            @NonNull WebRequest request) {
        String field = ex instanceof MethodArgumentTypeMismatchException mismatch
                ? mismatch.getName()
                : ex.getPropertyName();
        Map<String, List<String>> fieldErrors = Map.of(
                Objects.requireNonNullElse(field, GLOBAL_ERROR_KEY),
                List.of("Value has an invalid type or format")
        );
        ApiErrorResponse body = createErrorResponse(
                status,
                ApiErrorCode.INVALID_REQUEST_PARAMETER,
                INVALID_PARAMETER_MESSAGE,
                request,
                fieldErrors
        );
        return new ResponseEntity<>(body, headers, status);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, WebRequest request) {
        if (ex.getSuppressed().length > 0) {
            logger.error("Error during cleanup operation for request " + requestPath(request), ex);
        }
        HttpStatus status = HttpStatus.NOT_FOUND;
        ApiErrorResponse body = createErrorResponse(
                status,
                ApiErrorCode.RESOURCE_NOT_FOUND,
                "Requested resource was not found",
                request,
                Map.of()
        );
        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpectedException(
            Exception ex, WebRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        logUnexpectedException(ex, request);
        ApiErrorResponse errorResponse = createErrorResponse(
                status,
                ApiErrorCode.INTERNAL_ERROR,
                INTERNAL_ERROR_MESSAGE,
                request,
                Map.of()
        );
        return super.handleExceptionInternal(
                ex, errorResponse, HttpHeaders.EMPTY, status, request
        );
    }

    /**
     * Converts framework-level MVC exceptions to the API error format while preserving
     * Spring-provided status, headers, and internal error handling. The response body is
     * omitted for 406 because no acceptable representation is available.
     */
    @Override
    @NonNull
    protected ResponseEntity<Object> handleExceptionInternal(
            @NonNull Exception ex,
            Object ignoredBody,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatus status,
            @NonNull WebRequest request) {
        if (ex instanceof HttpMediaTypeNotAcceptableException) {
            return super.handleExceptionInternal(ex, null, headers, status, request);
        }
        ApiErrorCode code = ApiErrorCode.INVALID_REQUEST;
        String message = "Request could not be processed";
        if (ex instanceof HttpRequestMethodNotSupportedException) {
            code = ApiErrorCode.METHOD_NOT_ALLOWED;
            message = "Request method is not supported";
        } else if (ex instanceof HttpMediaTypeNotSupportedException) {
            code = ApiErrorCode.UNSUPPORTED_MEDIA_TYPE;
            message = "Request media type is not supported";
        } else if (status.is5xxServerError()) {
            logUnexpectedException(ex, request);
            code = ApiErrorCode.INTERNAL_ERROR;
            message = INTERNAL_ERROR_MESSAGE;
        }
        ApiErrorResponse errorResponse = createErrorResponse(
                status,
                code,
                message,
                request,
                Map.of()
        );
        return super.handleExceptionInternal(ex, errorResponse, headers, status, request);
    }

    private static ApiErrorResponse createErrorResponse(
            HttpStatus status,
            ApiErrorCode code,
            String message,
            WebRequest request,
            Map<String, List<String>> fieldErrors) {
        return new ApiErrorResponse(
                status.value(),
                code,
                message,
                requestPath(request),
                fieldErrors
        );
    }

    private static String requestPath(WebRequest request) {
        if (request instanceof ServletWebRequest servletWebRequest) {
            return servletWebRequest.getRequest().getRequestURI();
        }
        String description = request.getDescription(false);
        String uriPrefix = "uri=";
        return description.startsWith(uriPrefix)
                ? description.substring(uriPrefix.length())
                : description;
    }

    private static String fieldName(ConstraintViolation<?> violation) {
        String field = GLOBAL_ERROR_KEY;
        for (Path.Node node : violation.getPropertyPath()) {
            if ((node.getKind() == ElementKind.PARAMETER
                    || node.getKind() == ElementKind.PROPERTY)
                    && node.getName() != null
                    && !node.getName().isBlank()) {
                field = node.getName();
            }
        }
        return field;
    }

    private static String defaultMessage(DefaultMessageSourceResolvable error) {
        return Objects.requireNonNullElse(error.getDefaultMessage(), "Invalid value");
    }

    private static void addFieldError(
            Map<String, List<String>> fieldErrors, String field, String message) {
        fieldErrors.computeIfAbsent(field, ignored -> new ArrayList<>()).add(message);
    }

    private void logUnexpectedException(Exception ex, WebRequest request) {
        logger.error("Unexpected request failure for " + requestPath(request), ex);
    }
}
