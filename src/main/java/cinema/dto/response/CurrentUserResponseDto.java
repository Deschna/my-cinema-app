package cinema.dto.response;

import java.util.List;

public record CurrentUserResponseDto(String email, List<String> roles) {
    public CurrentUserResponseDto(String email, List<String> roles) {
        this.email = email;
        this.roles = List.copyOf(roles);
    }
}
