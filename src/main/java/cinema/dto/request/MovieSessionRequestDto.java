package cinema.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

public class MovieSessionRequestDto {
    @NotNull
    @Positive
    @Schema(description = "Movie ID from GET /movies", example = "1")
    private Long movieId;
    @NotNull
    @Positive
    @Schema(description = "Cinema hall ID from GET /cinema-halls", example = "1")
    private Long cinemaHallId;
    // Use a plain string schema so Swagger keeps the example without a time zone.
    @NotNull
    @Schema(implementation = String.class,
            description = "Session start date and time (yyyy-MM-ddTHH:mm)",
            example = "2030-01-15T19:30")
    private LocalDateTime showTime;

    public Long getMovieId() {
        return movieId;
    }

    public Long getCinemaHallId() {
        return cinemaHallId;
    }

    public LocalDateTime getShowTime() {
        return showTime;
    }
}
