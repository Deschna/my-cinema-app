package cinema.controller.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cinema.controller.MovieSessionController;
import cinema.dto.request.MovieSessionRequestDto;
import cinema.dto.response.MovieSessionResponseDto;
import cinema.model.MovieSession;
import cinema.service.MovieSessionService;
import cinema.service.mapper.RequestDtoMapper;
import cinema.service.mapper.ResponseDtoMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

class MovieSessionControllerParameterValidationTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final RequestParameterValidationFixture VALIDATION =
            new RequestParameterValidationFixture();

    @Test
    void validatesIdentifiersBeforeCallingMapperOrService() throws Exception {
        CountingMovieSessionService service = new CountingMovieSessionService();
        CountingMovieSessionMapper requestMapper = new CountingMovieSessionMapper();
        try (AnnotationConfigApplicationContext context = VALIDATION.controllerContext(
                MovieSessionController.class,
                () -> movieSessionController(service, requestMapper)
        )) {
            MockMvc mockMvc = VALIDATION.mockMvc(
                    context.getBean(MovieSessionController.class)
            );

            mockMvc.perform(get("/movie-sessions/available")
                            .param("movieId", "0")
                            .param("date", "13.10.2026"))
                    .andExpect(status().isBadRequest());
            assertEquals(0, service.findCallCount());

            mockMvc.perform(get("/movie-sessions/available")
                            .param("movieId", "1")
                            .param("date", "13.10.2026"))
                    .andExpect(status().isOk());
            assertEquals(1, service.findCallCount());

            MvcResult invalidDate = mockMvc.perform(get("/movie-sessions/available")
                            .param("movieId", "1")
                            .param("date", "not-a-date"))
                    .andExpect(status().isBadRequest())
                    .andReturn();
            JsonNode error = OBJECT_MAPPER.readTree(
                    invalidDate.getResponse().getContentAsByteArray()
            );
            assertEquals("INVALID_REQUEST_PARAMETER", error.path("code").asText());
            assertEquals("/movie-sessions/available", error.path("path").asText());
            assertEquals(
                    "Value has an invalid type or format",
                    error.path("fieldErrors").path("date").get(0).asText()
            );
            assertEquals(1, service.findCallCount());

            MvcResult missingDate = mockMvc.perform(get("/movie-sessions/available")
                            .param("movieId", "1"))
                    .andExpect(status().isBadRequest())
                    .andReturn();
            JsonNode missingParameterError = OBJECT_MAPPER.readTree(
                    missingDate.getResponse().getContentAsByteArray()
            );
            assertEquals(
                    "INVALID_REQUEST_PARAMETER",
                    missingParameterError.path("code").asText()
            );
            assertEquals(
                    "Required request parameter is missing",
                    missingParameterError.path("fieldErrors").path("date").get(0).asText()
            );
            assertEquals(1, service.findCallCount());

            mockMvc.perform(put("/movie-sessions/0")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validMovieSessionJson()))
                    .andExpect(status().isBadRequest());
            assertEquals(0, requestMapper.callCount());
            assertEquals(0, service.updateCallCount());

            mockMvc.perform(put("/movie-sessions/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validMovieSessionJson()))
                    .andExpect(status().isOk());
            assertEquals(1, requestMapper.callCount());
            assertEquals(1, service.updateCallCount());

            mockMvc.perform(delete("/movie-sessions/0"))
                    .andExpect(status().isBadRequest());
            assertEquals(0, service.deleteCallCount());

            mockMvc.perform(delete("/movie-sessions/1"))
                    .andExpect(status().isOk());
            assertEquals(1, service.deleteCallCount());
        }
    }

    @AfterAll
    static void closeValidatorFactory() {
        VALIDATION.close();
    }

    private static MovieSessionController movieSessionController(
            CountingMovieSessionService service,
            CountingMovieSessionMapper requestMapper) {
        ResponseDtoMapper<MovieSessionResponseDto, MovieSession> responseMapper =
                movieSession -> new MovieSessionResponseDto();
        return new MovieSessionController(service, requestMapper, responseMapper);
    }

    private static String validMovieSessionJson() {
        return "{\"movieId\":1,\"cinemaHallId\":1,"
                + "\"showTime\":\"2026-10-13T18:30:00\"}";
    }

    private static final class CountingMovieSessionMapper
            implements RequestDtoMapper<MovieSessionRequestDto, MovieSession> {
        private int calls;

        @Override
        public MovieSession mapToModel(MovieSessionRequestDto dto) {
            calls++;
            return new MovieSession();
        }

        int callCount() {
            return calls;
        }
    }

    private static final class CountingMovieSessionService implements MovieSessionService {
        private int findCalls;
        private int updateCalls;
        private int deleteCalls;

        @Override
        public List<MovieSession> findAvailableSessions(Long movieId, LocalDate date) {
            findCalls++;
            return List.of();
        }

        @Override
        public MovieSession add(MovieSession session) {
            throw new UnsupportedOperationException();
        }

        @Override
        public MovieSession get(Long id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public MovieSession update(MovieSession movieSession) {
            updateCalls++;
            return movieSession;
        }

        @Override
        public void delete(Long id) {
            deleteCalls++;
        }

        int findCallCount() {
            return findCalls;
        }

        int updateCallCount() {
            return updateCalls;
        }

        int deleteCallCount() {
            return deleteCalls;
        }
    }
}
