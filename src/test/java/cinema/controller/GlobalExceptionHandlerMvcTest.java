package cinema.controller;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cinema.config.WebConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.WebUtils;

/**
 * Verifies the shared API error contract through the Spring MVC pipeline.
 */
class GlobalExceptionHandlerMvcTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final LocalValidatorFactoryBean VALIDATOR = createValidator();
    private static final MockMvc MOCK_MVC = MockMvcBuilders
            .standaloneSetup(new ErrorProbeController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .setValidator(VALIDATOR)
            .build();

    @Test
    void returnsStableErrorForMalformedJson() throws Exception {
        String completeJson = "{\"value\":\"test\"}";
        String truncatedJson = completeJson.substring(0, completeJson.length() - 1);

        MvcResult result = MOCK_MVC.perform(post("/request-body")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(truncatedJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();

        JsonNode body = responseBody(result);
        assertErrorResponse(
                body,
                400,
                "MALFORMED_REQUEST_BODY",
                "Request body is malformed or unreadable",
                "/request-body"
        );
        assertTrue(body.path("fieldErrors").isEmpty());
    }

    @Test
    void returnsFieldErrorsForInvalidRequestBody() throws Exception {
        MvcResult result = MOCK_MVC.perform(post("/request-body")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();

        JsonNode body = responseBody(result);
        assertErrorResponse(
                body,
                400,
                "VALIDATION_ERROR",
                "Request validation failed",
                "/request-body"
        );
        JsonNode valueErrors = body.path("fieldErrors").path("value");
        assertEquals(1, body.path("fieldErrors").size());
        assertTrue(valueErrors.isArray());
        assertEquals(1, valueErrors.size());
        assertFalse(valueErrors.get(0).asText().isBlank());
    }

    @Test
    void returnsStableErrorForUnsupportedMethod() throws Exception {
        MvcResult result = MOCK_MVC.perform(get("/request-body"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();

        JsonNode body = responseBody(result);
        assertErrorResponse(
                body,
                405,
                "METHOD_NOT_ALLOWED",
                "Request method is not supported",
                "/request-body"
        );
        assertTrue(body.path("fieldErrors").isEmpty());
        assertEquals("POST", result.getResponse().getHeader(HttpHeaders.ALLOW));
    }

    @Test
    void returnsStableErrorForUnsupportedMediaType() throws Exception {
        MvcResult result = MOCK_MVC.perform(post("/request-body")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("request body"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();

        JsonNode body = responseBody(result);
        assertErrorResponse(
                body,
                415,
                "UNSUPPORTED_MEDIA_TYPE",
                "Request media type is not supported",
                "/request-body"
        );
        assertTrue(body.path("fieldErrors").isEmpty());
        String acceptedMediaTypes = result.getResponse().getHeader(HttpHeaders.ACCEPT);
        assertNotNull(acceptedMediaTypes);
        assertTrue(MediaType.parseMediaTypes(acceptedMediaTypes)
                .contains(MediaType.APPLICATION_JSON));
    }

    @Test
    void returnsGenericErrorForMissingRequiredHeader() throws Exception {
        MvcResult result = MOCK_MVC.perform(get("/required-header")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();

        JsonNode body = responseBody(result);
        assertErrorResponse(
                body,
                400,
                "INVALID_REQUEST",
                "Request could not be processed",
                "/required-header"
        );
        assertTrue(body.path("fieldErrors").isEmpty());
    }

    @Test
    void returnsNoBodyWhenClientDoesNotAcceptJson() throws Exception {
        MvcResult result = MOCK_MVC.perform(post("/request-body")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_XML)
                        .content("{\"value\":\"test\"}"))
                .andExpect(status().isNotAcceptable())
                .andReturn();

        assertEquals(0, result.getResponse().getContentAsByteArray().length);
    }

    @Test
    void hidesUnexpectedFailureDetails() throws Exception {
        // Application exceptions reach the catch-all @ExceptionHandler method.
        MvcResult result = MOCK_MVC.perform(get("/unexpected-failure")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();

        JsonNode body = responseBody(result);
        assertErrorResponse(
                body,
                500,
                "INTERNAL_ERROR",
                "An unexpected error occurred",
                "/unexpected-failure"
        );
        assertTrue(body.path("fieldErrors").isEmpty());
        assertFalse(result.getResponse().getContentAsString().contains("SQL state 08006"));
        assertInstanceOf(
                IllegalStateException.class,
                result.getRequest().getAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE)
        );
    }

    @Test
    void returnsSafeErrorForFrameworkServerFailure() throws Exception {
        // Spring MVC exceptions bypass the catch-all and reach handleExceptionInternal.
        MvcResult result = MOCK_MVC.perform(get("/framework-write-failure")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andReturn();

        JsonNode body = responseBody(result);
        assertErrorResponse(
                body,
                500,
                "INTERNAL_ERROR",
                "An unexpected error occurred",
                "/framework-write-failure"
        );
        assertTrue(body.path("fieldErrors").isEmpty());
        assertFalse(result.getResponse().getContentAsString().contains("Test-only write failure"));
        assertInstanceOf(
                HttpMessageNotWritableException.class,
                result.getRequest().getAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE)
        );
    }

    @AfterAll
    static void closeValidatorFactory() {
        VALIDATOR.destroy();
    }

    private static JsonNode responseBody(MvcResult result) throws Exception {
        return OBJECT_MAPPER.readTree(result.getResponse().getContentAsByteArray());
    }

    private static LocalValidatorFactoryBean createValidator() {
        LocalValidatorFactoryBean validator = new WebConfig().validator();
        validator.afterPropertiesSet();
        return validator;
    }

    private static void assertErrorResponse(JsonNode body, int status, String code,
                                            String message, String path) {
        assertTrue(body.isObject());
        assertEquals(6, body.size());
        assertTrue(body.path("timestamp").isTextual());
        assertDoesNotThrow(() -> Instant.parse(body.path("timestamp").asText()));
        assertTrue(body.path("status").isIntegralNumber());
        assertEquals(status, body.path("status").asInt());
        assertEquals(code, body.path("code").asText());
        assertEquals(message, body.path("message").asText());
        assertEquals(path, body.path("path").asText());
        assertTrue(body.path("fieldErrors").isObject());
    }

    @RestController
    @RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    private static final class ErrorProbeController {
        @PostMapping(value = "/request-body", consumes = MediaType.APPLICATION_JSON_VALUE)
        public ProbeRequest requestBody(@RequestBody @Valid ProbeRequest body) {
            return body;
        }

        @GetMapping("/required-header")
        public String requiredHeader(@RequestHeader("X-Request-Id") String requestId) {
            return requestId;
        }

        @GetMapping("/unexpected-failure")
        public void unexpectedFailure() {
            throw new IllegalStateException("SQL state 08006 while processing a request");
        }

        @GetMapping("/framework-write-failure")
        public void frameworkWriteFailure() {
            throw new HttpMessageNotWritableException("Test-only write failure");
        }
    }

    private record ProbeRequest(@NotBlank String value) {
    }
}
