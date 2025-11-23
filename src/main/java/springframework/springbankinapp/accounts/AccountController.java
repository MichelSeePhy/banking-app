package springframework.springbankinapp.accounts;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping("/debit")
    public ResponseEntity<DebitAccountResponseDto> createDebitAccount(@Valid @RequestBody CreateDebitAccountDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.createDebitAccount(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping("/credit")
    public ResponseEntity<CreditAccountResponseDto> createCreditAccount(@Valid @RequestBody CreateCreditAccountDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.createCreditAccount(request));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PatchMapping("/debit/{accountId}/limits")
    public ResponseEntity<?> updateDebitAccountLimit(
            @PathVariable Long accountId,
            @Valid @RequestBody UpdateDebitLimitsDto request) {

        accountService.updateDebitAccountLimit(accountId, request);
        return ResponseEntity.ok().build();

    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PatchMapping("/credit/{accountId}/limits")
    public ResponseEntity<?> updateCreditAccountLimit(
            @PathVariable Long accountId,
            @Valid @RequestBody UpdateCreditLimitsDto request) {

        accountService.updateCreditAccountLimit(accountId, request);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping("/{accountId}/close")
    public ResponseEntity<?> closeAccount(
            @PathVariable Long accountId) {
        accountService.closeAccount(accountId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @PostMapping("/{accountId}/status")
    public ResponseEntity<?> changeAccountStatus(
            @PathVariable Long accountId,
            @Valid @RequestBody ChangeAccountStatusRequest request) {
        accountService.changeAccountStatus(accountId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{accountNumber}")
    public AccountResponse getAccount(@PathVariable String accountNumber) {
        return accountService.getAccountByNumber(accountNumber);
    }

    @GetMapping
    public List<AccountResponse> getAllAccounts(
            @RequestParam(required = false) String number,
            @RequestParam(required = false) Type type
    ) {
        return accountService.getAllAccounts(number, type);
    }
}
