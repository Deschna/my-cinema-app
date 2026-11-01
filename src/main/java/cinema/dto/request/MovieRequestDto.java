package cinema.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class MovieRequestDto {
    @NotBlank
    @Size(max = 255)
    @Schema(example = "Interstellar")
    private String title;
    @Size(max = 200)
    @Schema(example = "A crew travels beyond Earth in search of a new home.")
    private String description;

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
