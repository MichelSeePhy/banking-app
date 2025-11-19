package springframework.springbankinapp.users;

import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public enum Role {
    ADMIN("ROLE_ADMIN"),
    USER("ROLE_USER"),
    MANAGER("ROLE_MANAGER");

    private final String authority;

    Role(String authority) {
        this.authority = authority;
    }

    private static final Map<String, Role> BY_AUTHORITY =
            Arrays.stream(values())
                    .collect(Collectors.toMap(Role::getAuthority, role -> role));

    public static Optional<Role> fromAuthority(String authority) {
        return Optional.ofNullable(BY_AUTHORITY.get(authority));
    }

    private static final Map<String, Role> BY_NAME =
            Arrays.stream(values())
                    .collect(Collectors.toMap(Enum::name, role -> role));

    public static Optional<Role> fromKeycloakRole(String roleName) {
        return Optional.ofNullable(BY_NAME.get(roleName));
    }
}
