package cinema.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cinema.dto.response.UserResponseDto;
import cinema.model.User;
import cinema.service.AuthenticationService;
import cinema.service.mapper.ResponseDtoMapper;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * Verifies that Spring MVC validates deserialized request bodies before invoking services.
 */
class RequestBodyValidationMvcTest {
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
                .setControllerAdvice(new CustomGlobalExceptionHandler())
                .setValidator(VALIDATOR)
                .build();
    }

    private static LocalValidatorFactoryBean createValidator() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.setMessageInterpolator(new ParameterMessageInterpolator());
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
