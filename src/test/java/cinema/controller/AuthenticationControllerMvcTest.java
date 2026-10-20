package cinema.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cinema.config.WebConfig;
import cinema.dto.response.UserResponseDto;
import cinema.model.User;
import cinema.service.AuthenticationService;
import cinema.service.mapper.ResponseDtoMapper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * Verifies request-body validation and response content negotiation on registration.
 * Rejected requests must not invoke the authentication service.
 */
class AuthenticationControllerMvcTest {
    private static final LocalValidatorFactoryBean VALIDATOR = createValidator();

    @Test
    void rejectsInvalidRegistrationWithoutCallingService() throws Exception {
        CountingAuthenticationService service = new CountingAuthenticationService();
        MockMvc mockMvc = registrationMockMvc(service);

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"password\","
                                + "\"repeatPassword\":\"password\"}"))
                .andExpect(status().isBadRequest());

        assertEquals(0, service.callCount());
    }

    @Test
    void rejectsUnacceptableResponseTypeBeforeCallingService() throws Exception {
        CountingAuthenticationService service = new CountingAuthenticationService();
        MockMvc mockMvc = registrationMockMvc(service);

        MvcResult result = mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_XML)
                        .content("{\"email\":\"user@example.com\","
                                + "\"password\":\"password\","
                                + "\"repeatPassword\":\"password\"}"))
                .andExpect(status().isNotAcceptable())
                .andReturn();

        assertEquals(0, result.getResponse().getContentAsByteArray().length);
        assertEquals(0, service.callCount());
    }

    @Test
    void acceptsValidRegistrationAndCallsServiceOnce() throws Exception {
        CountingAuthenticationService service = new CountingAuthenticationService();
        MockMvc mockMvc = registrationMockMvc(service);

        mockMvc.perform(post("/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"user@example.com\","
                                + "\"password\":\"password\","
                                + "\"repeatPassword\":\"password\"}"))
                .andExpect(status().isOk());

        assertEquals(1, service.callCount());
    }

    @AfterAll
    static void closeValidatorFactory() {
        VALIDATOR.destroy();
    }

    private static MockMvc registrationMockMvc(CountingAuthenticationService service) {
        ResponseDtoMapper<UserResponseDto, User> mapper = user -> {
            UserResponseDto responseDto = new UserResponseDto();
            responseDto.setEmail(user.getEmail());
            return responseDto;
        };
        return MockMvcBuilders.standaloneSetup(new AuthenticationController(service, mapper))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(VALIDATOR)
                .build();
    }

    private static LocalValidatorFactoryBean createValidator() {
        LocalValidatorFactoryBean validator = new WebConfig().validator();
        validator.afterPropertiesSet();
        return validator;
    }

    private static final class CountingAuthenticationService implements AuthenticationService {
        private int calls;

        @Override
        public User register(String email, String password) {
            calls++;
            User user = new User();
            user.setEmail(email);
            return user;
        }

        int callCount() {
            return calls;
        }
    }
}
