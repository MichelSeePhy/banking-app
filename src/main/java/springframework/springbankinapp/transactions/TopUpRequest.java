package springframework.springbankinapp.transactions;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TopUpRequest {

    @Min(value = 5, message = "Amount must be greater than 5")
    @Positive
    @NotNull
    private BigDecimal amount;

    @NotNull
    String targetAccountNumber;

}
