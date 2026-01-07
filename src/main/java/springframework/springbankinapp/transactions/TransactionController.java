package springframework.springbankinapp.transactions;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/top-up")
    public ResponseEntity<?> createTransaction(@Valid @RequestBody TopUpRequest transaction) {
        transactionService.topUp(transaction);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> createWithdrawal(@Valid @RequestBody WithdrawalRequest withdrawal) {
        transactionService.withdraw(withdrawal);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> createTransfer(@Valid @RequestBody TransferRequest transfer) {
        transactionService.transfer(transfer);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @GetMapping
    public List<TransactionSummary> getAllTransactions() {
        return transactionService.getAllTransactions();
    }
}
