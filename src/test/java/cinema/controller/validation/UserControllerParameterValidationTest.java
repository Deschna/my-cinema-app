package cinema.controller.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cinema.controller.UserController;
import cinema.dto.response.UserResponseDto;
import cinema.model.User;
import cinema.service.UserService;
import cinema.service.mapper.ResponseDtoMapper;
import java.util.Optional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.web.servlet.MockMvc;

class UserControllerParameterValidationTest {
    private static final RequestParameterValidationFixture VALIDATION =
            new RequestParameterValidationFixture();

    @Test
    void validatesEmailWithoutNormalizingIt() throws Exception {
        CountingUserService service = new CountingUserService();
        try (AnnotationConfigApplicationContext context = VALIDATION.controllerContext(
                UserController.class,
                () -> userController(service)
        )) {
            MockMvc mockMvc = VALIDATION.mockMvc(context.getBean(UserController.class));

            mockMvc.perform(get("/users/by-email").param("email", " "))
                    .andExpect(status().isBadRequest());
            mockMvc.perform(get("/users/by-email").param("email", " user@example.com"))
                    .andExpect(status().isBadRequest());
            assertEquals(0, service.findCallCount());

            mockMvc.perform(get("/users/by-email").param("email", "User@example.com"))
                    .andExpect(status().isOk());
            assertEquals(1, service.findCallCount());
            assertEquals("User@example.com", service.lastEmail());
        }
    }

    @AfterAll
    static void closeValidatorFactory() {
        VALIDATION.close();
    }

    private static UserController userController(CountingUserService service) {
        ResponseDtoMapper<UserResponseDto, User> mapper = user -> {
            UserResponseDto responseDto = new UserResponseDto();
            responseDto.setEmail(user.getEmail());
            return responseDto;
        };
        return new UserController(service, mapper);
    }

    private static final class CountingUserService implements UserService {
        private int findCalls;
        private String lastEmail;

        @Override
        public User add(User user) {
            throw new UnsupportedOperationException();
        }

        @Override
        public User get(Long id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<User> findByEmail(String email) {
            findCalls++;
            lastEmail = email;
            User user = new User();
            user.setEmail(email);
            return Optional.of(user);
        }

        int findCallCount() {
            return findCalls;
        }

        String lastEmail() {
            return lastEmail;
        }
    }
}
