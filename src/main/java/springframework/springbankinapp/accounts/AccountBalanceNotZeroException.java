package springframework.springbankinapp.accounts;

public class AccountBalanceNotZeroException extends RuntimeException {
    public AccountBalanceNotZeroException() {
        super("Account balance must be zero");
    }
}
