package springframework.springbankinapp.accounts;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateCreditLimitsDto {
    @PositiveOrZero
    private BigDecimal creditLimit;

    @Positive
    @Min(1)
    private Integer interest;
}
