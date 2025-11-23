package springframework.springbankinapp.accounts;

public class AccountNotActiveException extends RuntimeException{
    public AccountNotActiveException() {
        super("Account is blocked or closed");
    }
}
