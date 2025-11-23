package springframework.springbankinapp.accounts;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class UpdateDebitLimitsDto {
    @PositiveOrZero
    private BigDecimal operationLimit;
}
