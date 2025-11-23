package springframework.springbankinapp.transactions;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransferRequest implements TransactionOperation{

    @NotNull(message = "Source number is required")
    String sourceAccountNumber;

    @NotNull(message = "Target number is required")
    String targetAccountNumber;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive value")
    @Min(value = 5, message = "Amount must be greater than 5")
    BigDecimal amount;
}
