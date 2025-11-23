package springframework.springbankinapp.transactions;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WithdrawalRequest implements TransactionOperation{

    @NotNull(message = "Account number is required")
    private String sourceAccountNumber;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than 0")
    @Min(value = 5, message = "Amount must be greater than 5")
    private BigDecimal amount;
}
