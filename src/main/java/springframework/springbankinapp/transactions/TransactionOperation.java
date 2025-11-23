package springframework.springbankinapp.transactions;

import java.math.BigDecimal;

public interface TransactionOperation {

    BigDecimal getAmount();

    String getSourceAccountNumber();

}
