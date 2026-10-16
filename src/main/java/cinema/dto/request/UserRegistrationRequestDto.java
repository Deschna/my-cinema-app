package cinema.dto.request;

import cinema.validation.EmailFormat;
import cinema.validation.FieldsMatch;
import cinema.validation.Utf8ByteLength;
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
    private String email;
    @NotBlank
    @Size(min = 8, max = 40)
    @Utf8ByteLength(max = 72)
    private String password;
    @NotBlank
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
