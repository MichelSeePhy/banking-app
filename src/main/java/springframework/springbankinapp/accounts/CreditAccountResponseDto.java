package springframework.springbankinapp.accounts;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreditAccountResponseDto implements AccountResponse {

    private Long id;

    private String number;

    private Type type;

    private Status status;

    private BigDecimal balance;

    private BigDecimal creditLimit;

    private Integer interest;

    private LocalDateTime createdAt;

    private Long customerId;

}
