package cinema.dto.request;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

class MovieRequestDtoValidationTest {
    private static final int MAX_TITLE_LENGTH = 255;
    private static final int MAX_DESCRIPTION_LENGTH = 200;
    private static final RequestDtoValidationFixture VALIDATION =
            new RequestDtoValidationFixture();

    @Test
    void enforcesMovieTextLimits() throws JsonProcessingException {
        VALIDATION.assertViolationOnProperty(VALIDATION.validateJson(
                "{}",
                MovieRequestDto.class
        ), "title");
        VALIDATION.assertViolationOnProperty(VALIDATION.validateJson(
                "{\"title\":\"   \"}",
                MovieRequestDto.class
        ), "title");
        assertTrue(VALIDATION.validateJson(
                "{\"title\":\"a\"}",
                MovieRequestDto.class
        ).isEmpty());
        assertTrue(VALIDATION.validateJson(
                movieJson(
                        "a".repeat(MAX_TITLE_LENGTH),
                        "b".repeat(MAX_DESCRIPTION_LENGTH)
                ),
                MovieRequestDto.class
        ).isEmpty());
        VALIDATION.assertViolationOnProperty(VALIDATION.validateJson(
                movieJson("a".repeat(MAX_TITLE_LENGTH + 1), "description"),
                MovieRequestDto.class
        ), "title");
        VALIDATION.assertViolationOnProperty(VALIDATION.validateJson(
                movieJson("title", "b".repeat(MAX_DESCRIPTION_LENGTH + 1)),
                MovieRequestDto.class
        ), "description");
    }

    @AfterAll
    static void closeValidationFixture() {
        VALIDATION.close();
    }

    private static String movieJson(String title, String description)
            throws JsonProcessingException {
        return VALIDATION.toJson(Map.of(
                "title", title,
                "description", description
        ));
    }
}
