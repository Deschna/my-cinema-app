package cinema.config;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

import cinema.security.JsonAccessDeniedHandler;
import cinema.security.JsonAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final JsonAuthenticationEntryPoint authenticationEntryPoint;
    private final JsonAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(JsonAuthenticationEntryPoint authenticationEntryPoint,
                          JsonAccessDeniedHandler accessDeniedHandler) {
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    // Spring Security uses the existing UserDetailsService and PasswordEncoder beans.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Use explicit Ant matchers because MVC is configured in a separate child context.
        http.authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(antMatcher(HttpMethod.POST, "/register")).permitAll()
                        .requestMatchers(
                                antMatcher(HttpMethod.GET, "/movies"),
                                antMatcher(HttpMethod.GET, "/cinema-halls"),
                                antMatcher(HttpMethod.GET, "/movie-sessions/available"))
                        .hasAnyRole("ADMIN", "USER")
                        .requestMatchers(
                                antMatcher(HttpMethod.HEAD, "/movies"),
                                antMatcher(HttpMethod.HEAD, "/cinema-halls"),
                                antMatcher(HttpMethod.HEAD, "/movie-sessions/available"))
                        .hasAnyRole("ADMIN", "USER")
                        .requestMatchers(
                                antMatcher(HttpMethod.POST, "/movies"),
                                antMatcher(HttpMethod.POST, "/cinema-halls"),
                                antMatcher(HttpMethod.POST, "/movie-sessions"))
                        .hasRole("ADMIN")
                        .requestMatchers(antMatcher(HttpMethod.PUT, "/movie-sessions/{id}"))
                        .hasRole("ADMIN")
                        .requestMatchers(antMatcher(HttpMethod.DELETE, "/movie-sessions/{id}"))
                        .hasRole("ADMIN")
                        .requestMatchers(antMatcher(HttpMethod.GET, "/users/by-email"))
                        .hasRole("ADMIN")
                        .requestMatchers(antMatcher(HttpMethod.HEAD, "/users/by-email"))
                        .hasRole("ADMIN")
                        .requestMatchers(
                                antMatcher(HttpMethod.GET, "/orders"),
                                antMatcher(HttpMethod.GET, "/shopping-carts/by-user"))
                        .hasRole("USER")
                        .requestMatchers(
                                antMatcher(HttpMethod.HEAD, "/orders"),
                                antMatcher(HttpMethod.HEAD, "/shopping-carts/by-user"))
                        .hasRole("USER")
                        .requestMatchers(antMatcher(HttpMethod.POST, "/orders/complete"))
                        .hasRole("USER")
                        .requestMatchers(
                                antMatcher(HttpMethod.PUT, "/shopping-carts/movie-sessions"))
                        .hasRole("USER")
                        .anyRequest().denyAll())
                .formLogin(FormLoginConfigurer::permitAll)
                .httpBasic(basic -> basic.authenticationEntryPoint(authenticationEntryPoint))
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedHandler(accessDeniedHandler))
                .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }
}
