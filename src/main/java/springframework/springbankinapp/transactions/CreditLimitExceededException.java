package springframework.springbankinapp.transactions;

public class CreditLimitExceededException extends RuntimeException {
    public CreditLimitExceededException() {
        super("Credit limit exceeded");
    }
}
