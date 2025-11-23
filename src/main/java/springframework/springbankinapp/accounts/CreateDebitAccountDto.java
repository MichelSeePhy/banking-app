package springframework.springbankinapp.accounts;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreateDebitAccountDto {
    @PositiveOrZero
    BigDecimal operationLimit;

    @NotNull
    Long customerId;
}