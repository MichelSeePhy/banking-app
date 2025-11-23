package springframework.springbankinapp.accounts;

public class AccountTypeMismatchException extends RuntimeException{
    public AccountTypeMismatchException() {
        super("Account you're trying to update has invalid type");
    }
}
