package springframework.springbankinapp.accounts;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateCreditAccountDto {
    @PositiveOrZero
    BigDecimal creditLimit;

    @Min(1)
    @Positive
    Integer interest;

    @NotNull
    Long customerId;
}