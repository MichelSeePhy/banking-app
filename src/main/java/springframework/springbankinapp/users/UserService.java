package springframework.springbankinapp.users;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.*;
import springframework.springbankinapp.auth.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final KeycloakService keycloakService;
    private final AuthService authService;

    @Transactional
    public UserResponseDto createUser(CreateUserRequest request) {
        userRepository.findUserByEmail(request.getEmail()).ifPresent(u -> {
            throw new UserAlreadyExistsException();
        });

        String keycloakUserId = keycloakService.createUser(request);

        var userDetails = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .keycloakUserId(keycloakUserId)
                .build();

        var savedUser = userRepository.save(userDetails);

        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {
                        if (status == STATUS_ROLLED_BACK) {
                            log.warn("Transaction rolled back, deleting Keycloak user: {}", keycloakUserId);
                            keycloakService.deleteUser(keycloakUserId);
                        }
                    }
                }
        );

        return userMapper.toUserResponseDto(savedUser);
    }

    public UserResponseDto getUserById(Long userId) {
        var email = authService.getCurrentUserEmail();
        var currentUserId = userRepository.findUserByEmail(email)
                .map(User::getId)
                .orElseThrow();
        if (!currentUserId.equals(userId) && !authService.isAdmin()) {
            throw new AccessDeniedException("Access denied");
        }
        return userRepository
                .getUserById(userId)
                .map(userMapper::toUserResponseDto)
                .orElseThrow(UserNotFoundException::new);
    }

    @Transactional
    public void deleteUser(Long userId) {
        if (!authService.isAdmin()) {
            throw new AccessDeniedException("Access denied");
        }
        var user = userRepository.getUserById(userId)
                .orElseThrow(UserNotFoundException::new);

        userRepository.delete(user);
        keycloakService.deleteUser(user.getKeycloakUserId());

    }
}

