package springframework.springbankinapp.transactions;

import java.math.BigDecimal;
import java.util.UUID;

public interface TransactionSummary {

    UUID getId();
    BigDecimal getAmount();
    TransactionType getTransactionType();

}
