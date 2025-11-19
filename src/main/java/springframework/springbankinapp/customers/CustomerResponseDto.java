package springframework.springbankinapp.customers;

import lombok.Data;

@Data
public class CustomerResponseDto {

    private Long id;
    private String name;
    private String address;
    private String phoneNumber;
    private Type type;
}
