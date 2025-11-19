package springframework.springbankinapp.users;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
public class UpdateUserDto {

    @Size(min = 1, max = 16)
    private String firstName;
    @Size(min = 1, max = 16)
    private String lastName;
    @Email
    private String email;
    @Size(min = 13, max = 13)
    private String phoneNumber;

    private Boolean active;


}
