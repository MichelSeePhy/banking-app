package springframework.springbankinapp.accounts;

import jakarta.validation.Valid;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    @Mapping(source = "customer.id", target = "customerId")
    CreditAccountResponseDto toCreditDto(Account account);

    @Mapping(source = "customer.id", target = "customerId")
    DebitAccountResponseDto toDebitDto(Account account);

    Account debitToEntity(CreateDebitAccountDto createAccountDto);

    Account creditToEntity(@Valid CreateCreditAccountDto request);
}
