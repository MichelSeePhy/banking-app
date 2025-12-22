package springframework.springbankinapp.accounts;

public class InvalidStatusTransitionException extends RuntimeException {
    public InvalidStatusTransitionException() {
        super("Invalid status transition");
    }
}
