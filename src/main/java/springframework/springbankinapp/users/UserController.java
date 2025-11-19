package springframework.springbankinapp.users;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(
            @Valid @RequestBody CreateUserDto user
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(user));
    }

    @GetMapping("/{userId}")
    public UserResponseDto getUser(@PathVariable Long userId) {
        return userService.getUserById(userId);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserDto updateRequest) {
        userService.updateUser(userId, updateRequest);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{userId}/{action}")
    public ResponseEntity<?> setActiveForUser(
            @PathVariable Long userId,
            @PathVariable String action) {
        userService.setActive(userId, action);

        return ResponseEntity.ok().build();
    }

    @GetMapping()
    public List<UserResponseDto> getAllUsers(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) String firstName) {

        return userService.getAllUsers(email, lastName, firstName);
    }

    @PostMapping("/{userId}/change-role")
    public ResponseEntity<?> changePassword(
            @PathVariable Long userId,
            @RequestBody ChangeRoleRequest changeRoleRequest) {

        userService.changeUserRole(userId,changeRoleRequest);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


}
