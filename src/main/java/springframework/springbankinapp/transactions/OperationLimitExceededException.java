package springframework.springbankinapp.transactions;

public class OperationLimitExceededException extends RuntimeException {
    public OperationLimitExceededException() {
        super("Operation limit exceeded");
    }
}
