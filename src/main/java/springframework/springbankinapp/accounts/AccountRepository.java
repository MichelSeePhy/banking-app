package springframework.springbankinapp.accounts;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.*;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {


    @Query("""
                SELECT a FROM Account a 
                WHERE a.number = :number 
                AND (
                    a.customer.id = :privateCustomerId 
                    OR a.customer.id IN :organizationCustomerIds
                )
            """)
    Optional<Account> findAccountByNumberWithAccess(
            @Param("number") String number,
            @Param("privateCustomerId") Long privateCustomerId,
            @Param("organizationCustomerIds") Set<Long> organizationCustomerIds
    );

    Optional<Account> findByNumber(String number);

    @Query("""
    SELECT a FROM Account a 
    WHERE (:number IS NULL OR a.number = :number) 
    AND (:type IS NULL OR a.type = :type)
""")
    List<Account> findAllByNumberAndType(
            @Param("number") String number,
            @Param("type") Type type
    );

    @Query("""
    SELECT a FROM Account a 
    WHERE (:number IS NULL OR a.number = :number) 
    AND (:type IS NULL OR a.type = :type)
    AND (a.customer.id = :privateCustomerId 
         OR a.customer.id IN :organizationCustomerIds)
""")
    List<Account> findAllAccountsByNumberAndTypeWithAccess(
            @Param("number") String number,
            @Param("type") Type type,
            @Param("privateCustomerId") Long privateCustomerId,
            @Param("organizationCustomerIds") Set<Long> organizationCustomerIds
    );

    @Query("""
    SELECT a FROM Account a 
    WHERE a.status = :status
    AND a.type = :type
    AND a.balance < 0
""")
    List<Account> findAllAccountsForInterestCharge(@Param("status") Status status,
                                                   @Param("type") Type type);

}