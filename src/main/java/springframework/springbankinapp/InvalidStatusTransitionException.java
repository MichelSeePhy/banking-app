package springframework.springbankinapp;

public class InvalidStatusTransitionException extends RuntimeException {
    public InvalidStatusTransitionException() {
        super("Invalid status transition");
    }
}
