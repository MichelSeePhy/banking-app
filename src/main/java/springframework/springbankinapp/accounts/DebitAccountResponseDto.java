package springframework.springbankinapp.accounts;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DebitAccountResponseDto implements AccountResponse{

    private Long id;

    private String number;

    private Type type;

    private Status status;

    private BigDecimal balance;

    private BigDecimal operationLimit;

    private LocalDateTime createdAt;

    private Long customerId;

}
