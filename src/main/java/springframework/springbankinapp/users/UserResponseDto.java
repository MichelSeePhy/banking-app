package springframework.springbankinapp.users;

import lombok.Data;

@Data
public class UserResponseDto {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;

}
