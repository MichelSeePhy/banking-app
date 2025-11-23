package springframework.springbankinapp.users;

import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import springframework.springbankinapp.auth.AuthService;
import springframework.springbankinapp.customers.Customer;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserAccessContextProvider {

    private final AuthService authService;

    public UserAccessContext getUserAccessContext() {
        var user = authService.getCurrentUser();

        Long privateCustomerId = user.getPrivateCustomer() != null
                ? user.getPrivateCustomer().getId()
                : null;

        Set<Long> orgCustomerIds = user.getOrganizationCustomers().stream()
                .map(Customer::getId)
                .collect(Collectors.toSet());

        return new UserAccessContext(privateCustomerId, orgCustomerIds);
    }

    public record UserAccessContext(
            Long privateCustomerId,
            Set<Long> organizationCustomerIds
    ) {}
}
