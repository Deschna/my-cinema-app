package cinema.dto.request;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

class CinemaHallRequestDtoValidationTest {
    private static final int MIN_CAPACITY = 10;
    private static final int MAX_DESCRIPTION_LENGTH = 200;
    private static final RequestDtoValidationFixture VALIDATION =
            new RequestDtoValidationFixture();

    @Test
    void enforcesCinemaHallLimits() throws JsonProcessingException {
        VALIDATION.assertViolationOnProperty(VALIDATION.validateJson(
                "{}",
                CinemaHallRequestDto.class
        ), "capacity");
        assertTrue(VALIDATION.validateJson(
                VALIDATION.toJson(Map.of("capacity", MIN_CAPACITY)),
                CinemaHallRequestDto.class
        ).isEmpty());
        assertTrue(VALIDATION.validateJson(
                cinemaHallJson(
                        MIN_CAPACITY,
                        "a".repeat(MAX_DESCRIPTION_LENGTH)
                ),
                CinemaHallRequestDto.class
        ).isEmpty());
        VALIDATION.assertViolationOnProperty(VALIDATION.validateJson(
                cinemaHallJson(MIN_CAPACITY - 1, "description"),
                CinemaHallRequestDto.class
        ), "capacity");
        VALIDATION.assertViolationOnProperty(VALIDATION.validateJson(
                cinemaHallJson(MIN_CAPACITY, "a".repeat(MAX_DESCRIPTION_LENGTH + 1)),
                CinemaHallRequestDto.class
        ), "description");
    }

    @AfterAll
    static void closeValidationFixture() {
        VALIDATION.close();
    }

    private static String cinemaHallJson(int capacity, String description)
            throws JsonProcessingException {
        return VALIDATION.toJson(Map.of(
                "capacity", capacity,
                "description", description
        ));
    }
}
