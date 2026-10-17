package cinema.controller.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cinema.controller.ShoppingCartController;
import cinema.dto.response.ShoppingCartResponseDto;
import cinema.model.MovieSession;
import cinema.model.ShoppingCart;
import cinema.model.User;
import cinema.service.MovieSessionService;
import cinema.service.ShoppingCartService;
import cinema.service.UserService;
import cinema.service.mapper.ResponseDtoMapper;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;

class ShoppingCartControllerParameterValidationTest {
    private static final RequestParameterValidationFixture VALIDATION =
            new RequestParameterValidationFixture();

    @Test
    void validatesMovieSessionIdBeforeCallingServices() throws Exception {
        CountingShoppingCartService shoppingCartService = new CountingShoppingCartService();
        CountingUserService userService = new CountingUserService();
        CountingMovieSessionService movieSessionService = new CountingMovieSessionService();
        try (AnnotationConfigApplicationContext context = VALIDATION.controllerContext(
                ShoppingCartController.class,
                () -> shoppingCartController(
                        shoppingCartService,
                        userService,
                        movieSessionService
                )
        )) {
            MockMvc mockMvc = VALIDATION.mockMvc(
                    context.getBean(ShoppingCartController.class)
            );
            Authentication authentication = authentication();

            mockMvc.perform(put("/shopping-carts/movie-sessions")
                            .principal(authentication)
                            .param("movieSessionId", "0"))
                    .andExpect(status().isBadRequest());
            assertEquals(0, userService.findCallCount());
            assertEquals(0, movieSessionService.getCallCount());
            assertEquals(0, shoppingCartService.addCallCount());

            mockMvc.perform(put("/shopping-carts/movie-sessions")
                            .principal(authentication)
                            .param("movieSessionId", "1"))
                    .andExpect(status().isOk());
            assertEquals(1, userService.findCallCount());
            assertEquals(1, movieSessionService.getCallCount());
            assertEquals(1, shoppingCartService.addCallCount());
        }
    }

    @AfterAll
    static void closeValidatorFactory() {
        VALIDATION.close();
    }

    private static ShoppingCartController shoppingCartController(
            CountingShoppingCartService shoppingCartService,
            CountingUserService userService,
            CountingMovieSessionService movieSessionService) {
        ResponseDtoMapper<ShoppingCartResponseDto, ShoppingCart> mapper =
                shoppingCart -> new ShoppingCartResponseDto();
        return new ShoppingCartController(
                shoppingCartService,
                userService,
                movieSessionService,
                mapper
        );
    }

    private static Authentication authentication() {
        UserDetails principal = org.springframework.security.core.userdetails.User
                .withUsername("user@example.com")
                .password("password")
                .authorities("ROLE_USER")
                .build();
        return new UsernamePasswordAuthenticationToken(
                principal,
                principal.getPassword(),
                principal.getAuthorities()
        );
    }

    private static final class CountingMovieSessionService implements MovieSessionService {
        private int getCalls;

        @Override
        public List<MovieSession> findAvailableSessions(Long movieId, LocalDate date) {
            throw new UnsupportedOperationException();
        }

        @Override
        public MovieSession add(MovieSession session) {
            throw new UnsupportedOperationException();
        }

        @Override
        public MovieSession get(Long id) {
            getCalls++;
            return new MovieSession();
        }

        @Override
        public MovieSession update(MovieSession movieSession) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void delete(Long id) {
            throw new UnsupportedOperationException();
        }

        int getCallCount() {
            return getCalls;
        }
    }

    private static final class CountingShoppingCartService implements ShoppingCartService {
        private int addCalls;

        @Override
        public void addSession(MovieSession movieSession, User user) {
            addCalls++;
        }

        @Override
        public ShoppingCart getByUser(User user) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void registerNewShoppingCart(User user) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear(ShoppingCart shoppingCart) {
            throw new UnsupportedOperationException();
        }

        int addCallCount() {
            return addCalls;
        }
    }

    private static final class CountingUserService implements UserService {
        private int findCalls;

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
            User user = new User();
            user.setEmail(email);
            return Optional.of(user);
        }

        int findCallCount() {
            return findCalls;
        }
    }
}
