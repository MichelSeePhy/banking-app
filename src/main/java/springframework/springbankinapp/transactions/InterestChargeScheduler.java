package springframework.springbankinapp.transactions;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import springframework.springbankinapp.accounts.*;

import java.math.*;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Component
public class InterestChargeScheduler {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void chargeInterest() {
        System.out.println("=== Interest charge started at " + LocalDateTime.now() + " ===");
        var accounts = accountRepository.findAllAccountsForInterestCharge(Status.ACTIVE, Type.CREDIT);
        System.out.println("Found " + accounts.size() + " accounts");
        accounts.forEach(this::processInterestCharge);

    }

    private void processInterestCharge(Account account) {
        BigDecimal dailyInterest = account.getBalance().abs()
                .multiply(BigDecimal.valueOf(account.getInterest()))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                .divide(BigDecimal.valueOf(365), 2, RoundingMode.HALF_UP);

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
