package springframework.springbankinapp.customers;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateCustomerDto {

    @NotBlank(message = "Name cannot be blank")
    private String name;

    @NotBlank(message = "Address cannot be blank")
    private String address;

    @Size(min = 13, max = 13)
    @NotBlank(message = "Phone number cannot be blank")
    private String phoneNumber;
}
