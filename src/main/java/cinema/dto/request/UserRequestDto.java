package cinema.dto.request;

import cinema.validation.EmailFormat;
import cinema.validation.FieldsMatch;
import javax.validation.constraints.Size;

@FieldsMatch(
        field = "password",
        matchingField = "repeatPassword",
        message = "Passwords do not match!"
)
public class UserRequestDto {
    @EmailFormat
    private String email;
    @Size(min = 8, max = 40)
    private String password;
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
