package springframework.springbankinapp.accounts;

public class AccountNotFoundException extends RuntimeException{
    public AccountNotFoundException() {
        super("Account not found");
    }
}
