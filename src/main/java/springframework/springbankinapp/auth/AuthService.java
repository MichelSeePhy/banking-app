package springframework.springbankinapp.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import springframework.springbankinapp.users.entities.User;
import springframework.springbankinapp.users.Role;
import springframework.springbankinapp.users.exceptions.UserNotFoundException;
import springframework.springbankinapp.users.repositories.UserRepository;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final UserRepository userRepository;

    public Jwt getCurrentJwt() {
        return (Jwt) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
    }

    public String getCurrentUserEmail() {
        return getCurrentJwt().getClaimAsString("email");
    }

    public User getCurrentUser() {
        String currentUserEmail = getCurrentUserEmail();
        return userRepository.findUserByEmail(currentUserEmail)
                .orElseThrow(UserNotFoundException::new);
    }

    public Optional<Role> getCurrentRole() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(Role::fromAuthority)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

}
