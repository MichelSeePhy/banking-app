package springframework.springbankinapp.customers;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    boolean existsByIdAndUsersId(Long id, Long usersId);

    @Query("SELECT COUNT(u) FROM User u JOIN u.organizationCustomers c WHERE c.id = :customerId")
    int countMembersByCustomerId(@Param("customerId") Long customerId);

    @Modifying
    @Query(value = "DELETE FROM user_customer WHERE user_id = :userId AND customer_id = :customerId",
            nativeQuery = true)
    void removeUserFromCustomer(@Param("userId") Long userId,
                                @Param("customerId") Long customerId);

    @Query("SELECT c FROM Customer c WHERE " +
            "(:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND " +
            "(:type IS NULL OR c.type = :type)")
    List<Customer> findAllByQuery(@Param("name") String name, @Param("type") Type type);

    @Query("SELECT DISTINCT c FROM Customer c " +
            "LEFT JOIN c.users u " +
            "WHERE ((" +
            "    (:privateCustomerId IS NOT NULL AND c.type = 'PRIVATE' AND c.id = :privateCustomerId)" +
            "    OR (c.type = 'ORGANIZATION' AND u.id = :userId)" +
            ") " +
            "AND (:name IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))) " +
            "AND (:type IS NULL OR c.type = :type))")
    List<Customer> findCustomersForUser(
            @Param("userId") Long userId,
            @Param("privateCustomerId") Long privateCustomerId,
            @Param("name") String name,
            @Param("type") Type type
    );

}
