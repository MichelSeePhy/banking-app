package springframework.springbankinapp.transactions;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    Transaction topUptoEntity(TopUpRequest transaction);

    Transaction withdrawToEntity(WithdrawalRequest withdrawal);

    Transaction transferToEntity(TransferRequest transfer);
}
