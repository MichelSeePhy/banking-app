package springframework.springbankinapp.users;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CreateUserDto {

    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 16)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 16)
    private String lastName;

    @Email(message = "Must be a valid email")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Size(min = 13, max = 13)
    private String phoneNumber;

    @NotBlank
    @Size(min = 5, max = 25, message = "Password must be between 5 and 25 characters")
    private String password;

}
