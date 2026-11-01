package cinema.controller;

import cinema.dto.request.MovieSessionRequestDto;
import cinema.dto.response.MovieSessionResponseDto;
import cinema.model.MovieSession;
import cinema.service.MovieSessionService;
import cinema.service.mapper.RequestDtoMapper;
import cinema.service.mapper.ResponseDtoMapper;
import cinema.util.DateTimePatternUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.Positive;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Movie sessions")
@RequestMapping(value = "/movie-sessions", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class MovieSessionController {
    private final MovieSessionService movieSessionService;
    private final RequestDtoMapper<MovieSessionRequestDto, MovieSession>
            movieSessionRequestDtoMapper;
    private final ResponseDtoMapper<MovieSessionResponseDto, MovieSession>
            movieSessionResponseDtoMapper;

    public MovieSessionController(MovieSessionService movieSessionService,
            RequestDtoMapper<MovieSessionRequestDto, MovieSession> movieSessionRequestDtoMapper,
            ResponseDtoMapper<MovieSessionResponseDto, MovieSession>
                                      movieSessionResponseDtoMapper) {
        this.movieSessionService = movieSessionService;
        this.movieSessionRequestDtoMapper = movieSessionRequestDtoMapper;
        this.movieSessionResponseDtoMapper = movieSessionResponseDtoMapper;
    }

    @PostMapping
    @Operation(summary = "Schedule a movie session (ADMIN)")
    public MovieSessionResponseDto add(@RequestBody @Valid MovieSessionRequestDto requestDto) {
        MovieSession movieSession = movieSessionRequestDtoMapper.mapToModel(requestDto);
        movieSessionService.add(movieSession);
        return movieSessionResponseDtoMapper.mapToDto(movieSession);
    }

    @GetMapping("/available")
    @Operation(summary = "Find sessions by movie and date (USER / ADMIN)")
    public List<MovieSessionResponseDto> findAvailableSessions(
            @RequestParam @Positive
            @Parameter(description = "Movie ID from GET /movies", example = "1") Long movieId,
            @RequestParam
            @Parameter(description = "Date in " + DateTimePatternUtil.DATE_PATTERN + " format",
                    schema = @Schema(implementation = String.class))
            @DateTimeFormat(pattern = DateTimePatternUtil.DATE_PATTERN)
            LocalDate date) {
        return movieSessionService.findAvailableSessions(movieId, date)
                .stream()
                .map(movieSessionResponseDtoMapper::mapToDto)
                .toList();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a movie session (ADMIN)")
    public MovieSessionResponseDto update(@PathVariable @Positive
                                          @Parameter(description = "Movie session ID from "
                                                  + "GET /movie-sessions/available", example = "1")
                                          Long id,
                                          @RequestBody @Valid MovieSessionRequestDto requestDto) {
        MovieSession movieSession = movieSessionRequestDtoMapper.mapToModel(requestDto);
        movieSession.setId(id);
        movieSessionService.update(movieSession);
        return movieSessionResponseDtoMapper.mapToDto(movieSession);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a movie session (ADMIN)")
    public void delete(@PathVariable @Positive
                       @Parameter(description = "Movie session ID from "
                               + "GET /movie-sessions/available", example = "1") Long id) {
        movieSessionService.delete(id);
    }
}
