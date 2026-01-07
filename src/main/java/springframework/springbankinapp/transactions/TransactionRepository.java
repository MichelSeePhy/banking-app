package springframework.springbankinapp.transactions;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<TransactionSummary> findAllTransactionsBy();

}