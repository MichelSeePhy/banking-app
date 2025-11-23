package springframework.springbankinapp.accounts;

public class ClosedAccountCannotBeModifiedException extends RuntimeException {
    public ClosedAccountCannotBeModifiedException() {
        super("Closed account cannot be modified");
    }
}
