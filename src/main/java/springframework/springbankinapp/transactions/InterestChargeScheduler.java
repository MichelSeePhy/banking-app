package springframework.springbankinapp.transactions;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import springframework.springbankinapp.accounts.*;

import java.math.*;

@RequiredArgsConstructor
@Component
public class InterestChargeScheduler {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal DAYS_IN_YEAR = BigDecimal.valueOf(365);
    private static final BigDecimal DAILY_RATE_DIVISOR = HUNDRED.multiply(DAYS_IN_YEAR);

    @Scheduled(cron = "${scheduler.interest-charge.cron}")
    @Transactional
    public void chargeInterest() {
        var accounts = accountRepository.findAllAccountsForInterestCharge(Status.ACTIVE, Type.CREDIT);
        accounts.forEach(this::processInterestCharge);

    }

    private void processInterestCharge(Account account) {
        BigDecimal dailyInterest = account.getBalance().abs()
                .multiply(BigDecimal.valueOf(account.getInterest()))
                .divide(DAILY_RATE_DIVISOR, 2, RoundingMode.HALF_UP);

        BigDecimal newBalance = account.getBalance().subtract(dailyInterest);
        account.setBalance(newBalance);

        if (newBalance.compareTo(account.getCreditLimit().negate()) < 0) {
            account.setStatus(Status.BLOCKED);
        }

        var transaction = new Transaction();
        transaction.setAmount(dailyInterest);
        transaction.setTransactionType(TransactionType.INTEREST_CHARGE);
        transaction.setSourceAccount(account);

        transactionRepository.save(transaction);
    }
}
