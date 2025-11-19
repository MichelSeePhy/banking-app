package springframework.springbankinapp.customers;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import springframework.springbankinapp.auth.AuthService;
import springframework.springbankinapp.users.*;

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
        var email = authService.getCurrentUserEmail();
        var currentUser = userService.getUserByEmail(email);

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
        var email = authService.getCurrentUserEmail();
        var currentUser = userService.getUserByEmail(email);

        var customer = customerRepository.findById(customerId)
                .orElseThrow(CustomerNotFoundException::new);

        if (hasAccessToCustomer(currentUser, customer)) {
            return customerMapper.toCustomerResponseDto(customer);
        }

        throw new AccessDeniedException("You don't have permission to view this customer");
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

    @Transactional
    public void updateCustomer(Long customerId, UpdateCustomerDto updateRequest) {
        var email = authService.getCurrentUserEmail();
        var currentUser = userService.getUserByEmail(email);

        var customer = customerRepository.findById(customerId)
                .orElseThrow(CustomerNotFoundException::new);

        if (!hasAccessToCustomer(currentUser, customer)) {
            throw new AccessDeniedException("You don't have permission to update this customer");
        }
        customerMapper.updateEntityFromDto(updateRequest, customer);

    }


    @Transactional
    public void addMemberToOrgCustomer(Long customerId, @Valid AddMemberDto addMemberDto) {

        var customer = customerRepository.findById(customerId)
                .orElseThrow(CustomerNotFoundException::new);
        if (customer.getType() != Type.ORGANIZATION) {
            throw new IllegalArgumentException("Cannot add member to private customer");
        }
        var user = userRepository.getUserById(addMemberDto.getUserId())
                .orElseThrow(UserNotFoundException::new);

        var role = authService.getCurrentRole().orElseThrow();
        if (!canManageMembers(role)) {
            throw new AccessDeniedException("You don't have permission to add member to this organization");
        }

        if (customerRepository.existsByIdAndUsersId(customerId, addMemberDto.getUserId())) {
            throw new IllegalArgumentException("User is already organization member");
        }

        user.getOrganizationCustomers().add(customer);
    }

    private boolean canManageMembers(Role role) {
        return role == Role.ADMIN || role == Role.MANAGER;
    }

    @Transactional
    public void removeMemberFromOrganization(Long customerId, Long memberId) {

        var customer = customerRepository.findById(customerId)
                .orElseThrow(CustomerNotFoundException::new);
        if (customer.getType() != Type.ORGANIZATION) {
            throw new IllegalArgumentException("Cannot remove member from private customer");
        }
        if (!canManageMembers(authService.getCurrentRole().orElseThrow())) {
            throw new AccessDeniedException("You don't have permission to remove member from this organization");
        }
        if (!customerRepository.existsByIdAndUsersId(customerId, memberId)) {
            throw new IllegalArgumentException("User is not a member of this organization");
        }
        if (customerRepository.countMembersByCustomerId(customerId) == 1) {
            throw new IllegalArgumentException("Cannot remove last organization member");
        }

        customerRepository.removeUserFromCustomer(memberId, customerId);

    }


    @Transactional
    public void deleteCustomer(Long customerId) {
        var customer = customerRepository.findById(customerId)
                .orElseThrow(CustomerNotFoundException::new);

        var role = authService.getCurrentRole().orElseThrow();

        if (customer.getType() == Type.PRIVATE) {
            var owner = userRepository.findByPrivateCustomerId(customerId)
                    .orElseThrow(UserNotFoundException::new);

            var currentUserEmail = authService.getCurrentUserEmail();

            if (!canDeletePrivateCustomer(currentUserEmail, owner, role)) {
                throw new AccessDeniedException("You don't have permission to delete this customer");
            }

            owner.setPrivateCustomer(null);
            userRepository.save(owner);

        } else {
            if (role != Role.ADMIN) {
                throw new AccessDeniedException("Only ADMIN can delete organizations");
            }
            customerRepository.delete(customer);
        }
    }


    private boolean canDeletePrivateCustomer(String currentUserEmail, User owner, Role role) {
        return role == Role.ADMIN || role == Role.MANAGER
                || currentUserEmail.equalsIgnoreCase(owner.getEmail());
    }

    public List<CustomerResponseDto> getAllCustomers(String name, Type type) {
        var email = authService.getCurrentUserEmail();
        var currentUser = userService.getUserByEmail(email);

        Long privateCustomerId = currentUser.getPrivateCustomer() != null
                ? currentUser.getPrivateCustomer().getId()
                : null;
        List<Customer> list;
        if (authService.getCurrentRole().orElseThrow() == Role.USER) {
            list = customerRepository.findCustomersForUser(currentUser.getId(), privateCustomerId, name, type);
        } else {
            list = customerRepository.findAllByQuery(name, type);
        }

        return customerMapper.toCustomerResponseList(list);

    }
}




