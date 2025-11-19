package springframework.springbankinapp.users;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import springframework.springbankinapp.auth.*;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final KeycloakService keycloakService;

    public boolean canUpdateUser(User currentUser, User targetUser) {
        Role currentRole = authService.getCurrentRole().orElseThrow();

        if (currentRole == Role.USER) {
            return currentUser.getId().equals(targetUser.getId());
        }

        if (currentRole == Role.ADMIN) {
            return true;
        }

        if (currentRole == Role.MANAGER) {
            Role targetRole = keycloakService.getUserRoleByEmail(targetUser.getEmail());
            return targetRole != Role.ADMIN;
        }

        return false;
    }

    public boolean canChangeUserActiveState(User currentUser, User targetUser) {
        if (currentUser.getId().equals(targetUser.getId())) {
            return false;
        }

        Role currentRole = authService.getCurrentRole().orElseThrow();

        if (currentRole == Role.USER) {
            return false;
        }

        if (currentRole == Role.ADMIN) {
            return true;
        }

        if (currentRole == Role.MANAGER) {
            Role targetRole = keycloakService.getUserRoleByEmail(targetUser.getEmail());
            return targetRole == Role.USER;
        }

        return false;
    }

    public boolean canViewUser(User currentUser, User targetUser) {

        Role currentRole = authService.getCurrentRole().orElseThrow();

        if (currentRole == Role.ADMIN) {
            return true;
        }
        if (currentRole == Role.MANAGER) {
            Role targetRole = getTargetUserRole(targetUser.getId());
            return targetRole == Role.USER || currentUser.getId().equals(targetUser.getId());
        }

        return false;
    }

    public boolean canViewUsers() {
        Role currentRole = authService.getCurrentRole().orElseThrow();

        return currentRole != Role.USER;
    }

    public boolean canDeleteUser(User currentUser, User targetUser) {
        return canChangeUserActiveState(currentUser, targetUser);
    }

    private Role getTargetUserRole(Long userId) {
        User targetUser = userRepository.getUserById(userId)
                .orElseThrow(UserNotFoundException::new);

        return keycloakService.getUserRoleByEmail(targetUser.getEmail());
    }

    public boolean canChangeRole(User user) {
        Role currentRole = authService.getCurrentRole().orElseThrow();

        return currentRole == Role.ADMIN;
    }
}
