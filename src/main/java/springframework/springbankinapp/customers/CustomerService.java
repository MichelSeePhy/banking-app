package springframework.springbankinapp.customers;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import springframework.springbankinapp.auth.AuthService;
import springframework.springbankinapp.users.Role;
import springframework.springbankinapp.users.entities.User;
import springframework.springbankinapp.users.exceptions.UserNotFoundException;
import springframework.springbankinapp.users.repositories.UserRepository;
import springframework.springbankinapp.users.services.UserService;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CustomerService {

    private final AuthService authService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Transactional
    public CustomerResponseDto createCustomer(CreateCustomerDto createCustomerDto) {
        var currentUser = authService.getCurrentUser();

        if (currentUser.getPrivateCustomer() != null && createCustomerDto.getType() == Type.PRIVATE) {
            throw new IllegalArgumentException("User already has a private customer");
        }

        Customer customer = customerMapper.toEntity(createCustomerDto);

        if (createCustomerDto.getType() == Type.ORGANIZATION) {
            currentUser.getOrganizationCustomers().add(customer);
        } else {
            currentUser.setPrivateCustomer(customer);
        }
        customerRepository.save(customer);
        userRepository.save(currentUser);

        return customerMapper.toCustomerResponseDto(customer);
    }

    @Transactional
    public CustomerResponseDto getCustomerById(Long customerId) {
        var currentUser = authService.getCurrentUser();
        var customer = getCustomerWithAccessCheck(customerId, currentUser);

        return customerMapper.toCustomerResponseDto(customer);
    }

    @Transactional
    public void updateCustomer(Long customerId, UpdateCustomerDto updateRequest) {
        var currentUser = authService.getCurrentUser();
        var customer = getCustomerWithAccessCheck(customerId, currentUser);

        customerMapper.updateEntityFromDto(updateRequest, customer);
    }

    @Transactional
    public void addMemberToOrgCustomer(Long customerId, @Valid AddMemberDto addMemberDto) {
        var customer = getOrganizationCustomer(customerId);

        var role = authService.getCurrentRole().orElseThrow();
        if (!canManageMembers(role)) {
            throw new AccessDeniedException("You don't have permission to add member to this organization");
        }

        if (customerRepository.existsByIdAndUsersId(customerId, addMemberDto.getUserId())) {
            throw new IllegalArgumentException("User is already organization member");
        }

        var user = userRepository.getUserById(addMemberDto.getUserId())
                .orElseThrow(UserNotFoundException::new);

        user.getOrganizationCustomers().add(customer);
    }

    @Transactional
    public void removeMemberFromOrganization(Long customerId, Long memberId) {
        Customer orgCustomer = customerRepository.findOrganizationCustomerById(customerId)
                .orElseThrow(CustomerNotFoundException::new);

        if (!canManageMembers(authService.getCurrentRole().orElseThrow())) {
            throw new AccessDeniedException("You don't have permission to remove member from this organization");
        }

        if (orgCustomer.getUsers().stream().noneMatch(u -> u.getId().equals(memberId))) {
            throw new IllegalArgumentException("User is not a member of this organization");
        }

        if (orgCustomer.getUsers().size() == 1) {
            throw new IllegalArgumentException("Cannot remove last organization member");
        }

        User user = userRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.getOrganizationCustomers().removeIf(c -> c.getId().equals(customerId));
    }

    @Transactional
    public void deleteCustomer(Long customerId) {
        var customer = customerRepository.findById(customerId)
                .orElseThrow(CustomerNotFoundException::new);

        var role = authService.getCurrentRole().orElseThrow();

        if (customer.getType() == Type.PRIVATE) {
            deletePrivateCustomer(customer, role);
        } else {
            deleteOrganizationCustomer(customer, role);
        }
    }

    public List<CustomerResponseDto> getAllCustomers(String name, Type type) {
        var currentUser = authService.getCurrentUser();
        var role = authService.getCurrentRole().orElseThrow();

        List<Customer> list;
        if (role == Role.USER) {
            Long privateCustomerId = currentUser.getPrivateCustomer() != null
                    ? currentUser.getPrivateCustomer().getId()
                    : null;
            list = customerRepository.findCustomersForUser(currentUser.getId(), privateCustomerId, name, type);
        } else {
            list = customerRepository.findAllByQuery(name, type);
        }

        return customerMapper.toCustomerResponseList(list);
    }


    private Customer getOrganizationCustomer(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(CustomerNotFoundException::new);

        if (customer.getType() != Type.ORGANIZATION) {
            throw new IllegalArgumentException("This operation is only available for organizations");
        }

        return customer;
    }

    private Customer getCustomerWithAccessCheck(Long customerId, User currentUser) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(CustomerNotFoundException::new);

        if (!hasAccessToCustomer(currentUser, customer)) {
            throw new AccessDeniedException("You don't have permission to view this customer");
        }

        return customer;
    }

    private void deletePrivateCustomer(Customer customer, Role role) {
        var owner = userRepository.findByPrivateCustomerId(customer.getId())
                .orElseThrow(UserNotFoundException::new);

        var currentUserEmail = authService.getCurrentUserEmail();

        if (!canDeletePrivateCustomer(currentUserEmail, owner, role)) {
            throw new AccessDeniedException("You don't have permission to delete this customer");
        }

        owner.setPrivateCustomer(null);
        userRepository.save(owner);
    }

    private void deleteOrganizationCustomer(Customer customer, Role role) {
        if (role != Role.ADMIN) {
            throw new AccessDeniedException("Only ADMIN can delete organizations");
        }
        customerRepository.delete(customer);
    }

    private boolean hasAccessToCustomer(User currentUser, Customer customer) {
        boolean isPrivateCustomer = currentUser.getPrivateCustomer() != null
                && currentUser.getPrivateCustomer().getId().equals(customer.getId());
        if (isPrivateCustomer) return true;

        return customerRepository.existsByIdAndUsersId(
                customer.getId(),
                currentUser.getId()
        );
    }

    private boolean canManageMembers(Role role) {
        return role == Role.ADMIN || role == Role.MANAGER;
    }

    private boolean canDeletePrivateCustomer(String currentUserEmail, User owner, Role role) {
        return role == Role.ADMIN || role == Role.MANAGER
                || currentUserEmail.equalsIgnoreCase(owner.getEmail());
    }
}





