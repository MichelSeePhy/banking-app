package springframework.springbankinapp.transactions.events;

import java.math.BigDecimal;

public record TransactionTopUpEvent(
        String targetAccountNumber,
        String transactionType,
        BigDecimal amount
) {}
