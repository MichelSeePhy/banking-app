package springframework.springbankinapp.accounts;

public class AccountAlreadyInStatusException extends RuntimeException {
    public AccountAlreadyInStatusException(Status newStatus) {
        super("Account is already in " + newStatus.name() + " status");
    }
}
