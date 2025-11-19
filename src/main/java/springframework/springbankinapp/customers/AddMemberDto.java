package springframework.springbankinapp.customers;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddMemberDto {

    @NotNull
    private Long userId;
}
