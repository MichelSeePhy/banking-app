package springframework.springbankinapp.transactions;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import springframework.springbankinapp.accounts.*;
import springframework.springbankinapp.users.UserAccessContextProvider;

import java.math.BigDecimal;


@RequiredArgsConstructor
@Service
public class TransactionService {
    private final TransactionMapper transactionMapper;
    private final AccountRepository accountRepository;
    private final UserAccessContextProvider contextProvider;
    private final TransactionRepository transactionRepository;

    @Transactional
    public void topUp(TopUpRequest request) {
        var context = contextProvider.getUserAccessContext();

        var targetAccount = accountRepository
                .findAccountByNumberWithAccess(request.getTargetAccountNumber(), context.privateCustomerId(), context.organizationCustomerIds())
                .orElseThrow(AccountNotFoundException::new);

        if (targetAccount.getStatus() != Status.ACTIVE) {
            throw new AccountNotActiveException();
        }
        var transaction = transactionMapper.topUptoEntity(request);
        transaction.setTransactionType(TransactionType.TOP_UP);
        transaction.setTargetAccount(targetAccount);
        targetAccount.setBalance(
                targetAccount.getBalance().add(request.getAmount())
        );

        transactionRepository.save(transaction);
    }

    @Transactional
    public void withdraw(WithdrawalRequest withdrawal) {

        var sourceAccount = getAccountWithAccess(withdrawal);

        if (sourceAccount.getStatus() != Status.ACTIVE) {
            throw new AccountNotActiveException();
        }

        if (sourceAccount.getType() == Type.DEBIT) {
            validateDebitOperation(sourceAccount, withdrawal);
        } else {
            validateCreditOperation(sourceAccount, withdrawal);
        }

        var transaction = transactionMapper.withdrawToEntity(withdrawal);
        transaction.setTransactionType(TransactionType.WITHDRAW);
        transaction.setSourceAccount(sourceAccount);
        sourceAccount.setBalance(sourceAccount.getBalance().subtract(withdrawal.getAmount()));

        transactionRepository.save(transaction);

    }

    @Transactional
    public void transfer(TransferRequest transfer) {

        var sourceAccount = getAccountWithAccess(transfer);

        if (sourceAccount.getStatus() != Status.ACTIVE) {
            throw new AccountNotActiveException();
        }
        var targetAccount = accountRepository.findByNumber(transfer.getTargetAccountNumber())
                .orElseThrow(AccountNotFoundException::new);

        if (targetAccount.getStatus() != Status.ACTIVE) {
            throw new AccountNotActiveException();
        }

        if (sourceAccount.getType() == Type.DEBIT) {
            validateDebitOperation(sourceAccount, transfer);
        } else {
            validateCreditOperation(sourceAccount, transfer);
        }

        sourceAccount.setBalance(sourceAccount.getBalance().subtract(transfer.getAmount()));
        targetAccount.setBalance(targetAccount.getBalance().add(transfer.getAmount()));

        var transaction = transactionMapper.transferToEntity(transfer);
        transaction.setTransactionType(TransactionType.TRANSFER);
        transaction.setSourceAccount(sourceAccount);
        transaction.setTargetAccount(targetAccount);
        transactionRepository.save(transaction);
    }

    private void validateCreditOperation(Account sourceAccount, TransactionOperation withdrawal) {
        BigDecimal newBalance = sourceAccount.getBalance().subtract(withdrawal.getAmount());
        BigDecimal minAllowedBalance = sourceAccount.getCreditLimit().negate();

        if (newBalance.compareTo(minAllowedBalance) < 0) {
            throw new CreditLimitExceededException();
        }

    }

    private void validateDebitOperation(Account sourceAccount, TransactionOperation withdrawal) {
        if (sourceAccount.getBalance().compareTo(withdrawal.getAmount()) < 0) {
            throw new InsufficientFundsException();
        }
        if (sourceAccount.getOperationLimit().compareTo(withdrawal.getAmount()) < 0) {
            throw new OperationLimitExceededException();
        }
    }

    private Account getAccountWithAccess(TransactionOperation transfer) {
        var context = contextProvider.getUserAccessContext();

        return accountRepository
                .findAccountByNumberWithAccess(transfer.getSourceAccountNumber(),
                        context.privateCustomerId(),
                        context.organizationCustomerIds())
                .orElseThrow(AccountNotFoundException::new);
    }
}
