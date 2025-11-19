package springframework.springbankinapp.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import springframework.springbankinapp.users.Role;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class AuthService {

    private final KeycloakService keycloakService;


    public Jwt getCurrentJwt() {
        return (Jwt) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
    }

    public String getCurrentUserEmail() {
        return getCurrentJwt().getClaimAsString("email");
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
