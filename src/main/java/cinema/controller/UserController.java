package cinema.controller;

import cinema.dto.response.UserResponseDto;
import cinema.exception.ResourceNotFoundException;
import cinema.model.User;
import cinema.service.UserService;
import cinema.service.mapper.ResponseDtoMapper;
import cinema.validation.EmailFormat;
import javax.validation.constraints.NotBlank;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class UserController {
    private final UserService userService;
    private final ResponseDtoMapper<UserResponseDto, User> userResponseDtoMapper;

    public UserController(UserService userService,
                          ResponseDtoMapper<UserResponseDto, User> userResponseDtoMapper) {
        this.userService = userService;
        this.userResponseDtoMapper = userResponseDtoMapper;
    }

    @GetMapping("/by-email")
    public UserResponseDto findByEmail(@RequestParam @NotBlank @EmailFormat String email) {
        User user = userService.findByEmail(email).orElseThrow(
                () -> new ResourceNotFoundException("User not found by email"));
        return userResponseDtoMapper.mapToDto(user);
    }
}
