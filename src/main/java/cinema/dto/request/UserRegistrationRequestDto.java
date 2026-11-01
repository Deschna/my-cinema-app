package cinema.dto.request;

import cinema.validation.EmailFormat;
import cinema.validation.FieldsMatch;
import cinema.validation.Utf8ByteLength;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@FieldsMatch(
        field = "password",
        matchingField = "repeatPassword",
        message = "Passwords do not match!"
)
public class UserRegistrationRequestDto {
    @NotBlank
    @EmailFormat
    @Schema(example = "viewer@example.com")
    private String email;
    @NotBlank
    @Size(min = 8, max = 40)
    @Utf8ByteLength(max = 72)
    @Schema(example = "CinemaDemo123!", format = "password")
    private String password;
    @NotBlank
    @Schema(example = "CinemaDemo123!", format = "password")
    private String repeatPassword;

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getRepeatPassword() {
        return repeatPassword;
    }
}
