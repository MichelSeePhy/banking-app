package springframework.springbankinapp.accounts;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import springframework.springbankinapp.*;
import springframework.springbankinapp.auth.AuthService;
import springframework.springbankinapp.customers.*;
import springframework.springbankinapp.users.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {


    private final AccountMapper accountMapper;
    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final AuthService authService;
    private final UserAccessContextProvider contextProvider;

    @Value("${account.defaults.debit.operation-limit}")
    private BigDecimal defaultDebitOperationLimit;

    @Value("${account.defaults.credit.credit-limit}")
    private BigDecimal defaultCreditLimit;

    @Value("${account.defaults.credit.interest}")
    private Integer defaultCreditInterest;

    public DebitAccountResponseDto createDebitAccount(@Valid CreateDebitAccountDto request) {

        var customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(CustomerNotFoundException::new);


        var limit = request.getOperationLimit() != null ? request.getOperationLimit() : defaultDebitOperationLimit;

        var account = accountMapper.debitToEntity(request);
        account.setNumber(generateAccountNumber());
        account.setType(Type.DEBIT);
        account.setCustomer(customer);
        account.setOperationLimit(limit);

        accountRepository.save(account);
        return accountMapper.toDebitDto(account);
    }

    public CreditAccountResponseDto createCreditAccount(@Valid CreateCreditAccountDto request) {

        var customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(CustomerNotFoundException::new);


        var limit = request.getCreditLimit() != null ? request.getCreditLimit() : defaultCreditLimit;
        var interest = request.getInterest() != null ? request.getInterest() : defaultCreditInterest;

        var account = accountMapper.creditToEntity(request);
        account.setNumber(generateAccountNumber());
        account.setType(Type.CREDIT);
        account.setCustomer(customer);
        account.setCreditLimit(limit);
        account.setInterest(interest);
        accountRepository.save(account);
        return accountMapper.toCreditDto(account);
    }

    private String generateAccountNumber() {
        String prefix = "UA21BANK";
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(0, 13);
        String randomNumber = String.format("%013d", ThreadLocalRandom.current().nextLong(10_000_000_000_000L));

        return prefix + timestamp + randomNumber;
    }

    @Transactional
    public void updateDebitAccountLimit(Long accountId, UpdateDebitLimitsDto request) {
        var account = getActiveAccountByType(accountId);
        validateAccountType(account, Type.DEBIT);
        account.setOperationLimit(request.getOperationLimit());
    }

    @Transactional
    public void updateCreditAccountLimit(Long accountId, UpdateCreditLimitsDto request) {
        var account = getActiveAccountByType(accountId);
        validateAccountType(account, Type.CREDIT);
        account.setCreditLimit(request.getCreditLimit());
        account.setInterest(request.getInterest());
    }

    private Account getActiveAccountByType(Long accountId) {
        var account = accountRepository.findById(accountId)
                .orElseThrow(AccountNotFoundException::new);

        if (account.getStatus() == Status.BLOCKED || account.getStatus() == Status.CLOSED) {
            throw new AccountNotActiveException();
        }

        return account;
    }

    private void validateAccountType(Account account, Type type) {
        if (!(account.getType() == type)) {
            throw new AccountTypeMismatchException();
        }
    }

    @Transactional
    public void closeAccount(Long accountId) {
        var account = getActiveAccountByType(accountId);

        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new AccountBalanceNotZeroException();
        }
        account.setStatus(Status.CLOSED);
    }

    @Transactional
    public void changeAccountStatus(Long accountId, ChangeAccountStatusRequest request) {
        var account = accountRepository.findById(accountId)
                .orElseThrow(AccountNotFoundException::new);
        Status newStatus = request.getAction() == AccountAction.BLOCK ? Status.BLOCKED : Status.ACTIVE;
        validateStatusChange(account, newStatus);
        account.setStatus(newStatus);
    }

    private void validateStatusChange(Account account, Status newStatus) {
        if (account.getStatus() == Status.CLOSED) {
            throw new ClosedAccountCannotBeModifiedException();
        }

        if (newStatus == Status.CLOSED) {
            throw new InvalidStatusTransitionException();
        }

        if (account.getStatus() == newStatus) {
            throw new AccountAlreadyInStatusException(newStatus);
        }
    }

    public AccountResponse getAccountByNumber(String number) {
        var role = authService.getCurrentRole().orElseThrow();
        Account account;

        if (role == Role.ADMIN || role == Role.MANAGER) {
            account = accountRepository.findByNumber(number)
                    .orElseThrow(AccountNotFoundException::new);
        } else {
            var context = contextProvider.getUserAccessContext();

            account = accountRepository.findAccountByNumberWithAccess(
                    number,
                    context.privateCustomerId(),
                    context.organizationCustomerIds()
            ).orElseThrow(AccountNotFoundException::new);
        }

        return mapToDto(account);
    }

    private AccountResponse mapToDto(Account account) {
        return switch (account.getType()) {
            case DEBIT -> accountMapper.toDebitDto(account);
            case CREDIT -> accountMapper.toCreditDto(account);
        };
    }

    public List<AccountResponse> getAllAccounts(String number, Type type) {

        var role = authService.getCurrentRole().orElseThrow();
        List<Account> accounts;

        if (role == Role.ADMIN || role == Role.MANAGER) {
            accounts = accountRepository.findAllByNumberAndType(number, type);
        } else {
            var context = contextProvider.getUserAccessContext();

            accounts = accountRepository.findAllAccountsByNumberAndTypeWithAccess(
                    number,
                    type,
                    context.privateCustomerId(),
                    context.organizationCustomerIds());
        }

        return accounts.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
}

