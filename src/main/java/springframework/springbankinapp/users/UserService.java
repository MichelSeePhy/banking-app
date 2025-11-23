package springframework.springbankinapp.users;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.*;
import springframework.springbankinapp.auth.*;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final KeycloakService keycloakService;
    private final AuthService authService;
    private final PermissionService permissionService;

    @Transactional
    public UserResponseDto createUser(CreateUserDto request) {
        userRepository.findUserByEmail(request.getEmail()).ifPresent(u -> {
            throw new UserAlreadyExistsException();
        });

        String keycloakUserId = keycloakService.createUser(request);

        var userDetails = new User();
        userDetails.setFirstName(request.getFirstName());
        userDetails.setLastName(request.getLastName());
        userDetails.setEmail(request.getEmail());
        userDetails.setPhoneNumber(request.getPhoneNumber());
        userDetails.setKeycloakUserId(keycloakUserId);


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

    @Transactional
    public void updateUser(Long userId, UpdateUserDto updateRequest) {
        User currentUser = authService.getCurrentUser();
        User userToUpdate = getUser(userId);

        if (!permissionService.canUpdateUser(currentUser, userToUpdate)) {
            throw new AccessDeniedException("You don't have permission to update this user");
        }

        keycloakService.updateUser(userToUpdate.getEmail(), updateRequest);
        userMapper.update(updateRequest, userToUpdate);
        userRepository.save(userToUpdate);
    }

    public UserResponseDto getUserById(Long userId) {
        User currentUser = authService.getCurrentUser();
        User targetUser = getUser(userId);
        if (!permissionService.canViewUser(currentUser, targetUser)) {
            throw new AccessDeniedException("You don't have permission to view this user");
        }

        return userMapper.toUserResponseDto(targetUser);
    }

    @Transactional
    public void setActive(Long userId, String action) {
        User currentUser = authService.getCurrentUser();
        User userToUpdate = getUser(userId);
        Boolean requestedActiveStatus = switch (action) {
            case "activate" -> true;
            case "deactivate" -> false;
            default -> throw new IllegalArgumentException("Invalid action: " + action);
        };
        if (!requestedActiveStatus.equals(userToUpdate.isActive())) {
            if (!permissionService.canChangeUserActiveState(currentUser, userToUpdate)) {
                throw new AccessDeniedException("You don't have permission to change user status");
            }
            userToUpdate.setActive(requestedActiveStatus);
            keycloakService.setUserEnabled(userToUpdate.getKeycloakUserId(), requestedActiveStatus);
            userRepository.save(userToUpdate);
        }
    }

    @Transactional
    public void deleteUser(Long userId) {
        User currentUser = authService.getCurrentUser();
        var targetUser = getUser(userId);
        if (!permissionService.canDeleteUser(currentUser, targetUser)) {
            throw new AccessDeniedException("You don't have permission to delete this user");
        }
        userRepository.delete(targetUser);
        keycloakService.deleteUser(targetUser.getKeycloakUserId());

    }

    private User getUser(Long userId) {
        return userRepository.getUserById(userId)
                .orElseThrow(UserNotFoundException::new);
    }

    public List<UserResponseDto> getAllUsers(String email, String lastName, String firstName) {
        if (!permissionService.canViewUsers()) {
            throw new AccessDeniedException("Access denied");
        }
        var userList = userRepository.findAllByQuery(email, lastName, firstName);
        return userMapper.toUserResponseList(userList);
    }

    @Transactional
    public void changeUserRole(Long userId, ChangeRoleRequest changeRoleRequest) {
        var targetUser = getUser(userId);
        if (!permissionService.canChangeRole()) {
            throw new AccessDeniedException("You don't have permission to change role");
        }

        var currentRole = keycloakService.getUserRoleByEmail(targetUser.getEmail());
        if (currentRole == changeRoleRequest.getNewRole()) {
            throw new IllegalStateException("User already has role: " + currentRole);
        }
        keycloakService.changeUserRole(
                targetUser.getKeycloakUserId(),
                currentRole.name(),
                changeRoleRequest.getNewRole().name()
        );

    }

    public User getUserByEmail(String email) {
        return userRepository.findUserByEmail(email)
                .orElseThrow(UserNotFoundException::new);
    }
}

