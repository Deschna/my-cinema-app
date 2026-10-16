package cinema.dto.request;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Set;
import javax.validation.ConstraintViolation;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

class MovieSessionRequestDtoValidationTest {
    private static final RequestDtoValidationFixture VALIDATION =
            new RequestDtoValidationFixture();

    @Test
    void rejectsMissingFieldsAndNonPositiveIds() throws JsonProcessingException {
        Set<ConstraintViolation<MovieSessionRequestDto>> missing = VALIDATION.validateJson(
                "{}",
                MovieSessionRequestDto.class
        );
        VALIDATION.assertViolationOnProperty(missing, "movieId");
        VALIDATION.assertViolationOnProperty(missing, "cinemaHallId");
        VALIDATION.assertViolationOnProperty(missing, "showTime");

        Set<ConstraintViolation<MovieSessionRequestDto>> zeroIds = VALIDATION.validateJson(
                "{\"movieId\":0,\"cinemaHallId\":0,"
                        + "\"showTime\":\"2026-10-13T18:30:00\"}",
                MovieSessionRequestDto.class
        );
        VALIDATION.assertViolationOnProperty(zeroIds, "movieId");
        VALIDATION.assertViolationOnProperty(zeroIds, "cinemaHallId");

        Set<ConstraintViolation<MovieSessionRequestDto>> negativeIds = VALIDATION.validateJson(
                "{\"movieId\":-1,\"cinemaHallId\":-1,"
                        + "\"showTime\":\"2026-10-13T18:30:00\"}",
                MovieSessionRequestDto.class
        );
        VALIDATION.assertViolationOnProperty(negativeIds, "movieId");
        VALIDATION.assertViolationOnProperty(negativeIds, "cinemaHallId");
    }

    @Test
    void acceptsValidRequest() throws JsonProcessingException {
        assertTrue(VALIDATION.validateJson(
                "{\"movieId\":1,\"cinemaHallId\":2,"
                        + "\"showTime\":\"2026-10-13T18:30:00\"}",
                MovieSessionRequestDto.class
        ).isEmpty());
    }

    @AfterAll
    static void closeValidationFixture() {
        VALIDATION.close();
    }
}
