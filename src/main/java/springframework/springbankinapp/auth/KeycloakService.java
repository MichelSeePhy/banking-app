package springframework.springbankinapp.auth;

import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import springframework.springbankinapp.users.*;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakService {

    private final Keycloak keycloak;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.default-role:USER}")
    private String defaultRole;

    public String createUser(CreateUserRequest request) {
        UserRepresentation user = new UserRepresentation();
        user.setEnabled(true);
        user.setUsername(request.getEmail());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmailVerified(true);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(request.getPassword());
        credential.setTemporary(false);
        user.setCredentials(List.of(credential));

        RealmResource realmResource = keycloak.realm(realm);
        UsersResource usersResource = realmResource.users();

        Response response = usersResource.create(user);

        if (response.getStatus() == 409) {
            throw new UserAlreadyExistsException();
        }

        if (response.getStatus() != 201) {
            throw new RuntimeException("Failed to create user in Keycloak: " + response.getStatusInfo());
        }

        String locationHeader = response.getLocation().toString();
        String userId = locationHeader.substring(locationHeader.lastIndexOf('/') + 1);
        response.close();

        try {
            assignUserRole(userId);
        } catch (Exception e) {
            log.error("Failed to assign role to user {}, cleaning up", userId, e);
            deleteUser(userId);
            throw new RuntimeException("Failed to assign user role", e);
        }

        return userId;
    }

    private void assignUserRole(String userId) {
        RealmResource realmResource = keycloak.realm(realm);
        UserResource userResource = realmResource.users().get(userId);

        RoleRepresentation userRole = realmResource.roles().get(defaultRole).toRepresentation();

        userResource.roles().realmLevel().add(List.of(userRole));

        log.info("Assigned USER role to user: {}", userId);
    }

    public void deleteUser(String keycloakUserId) {
        try {
            keycloak.realm(realm).users().get(keycloakUserId).remove();
            log.info("Deleted Keycloak user: {}", keycloakUserId);
        } catch (Exception e) {
            log.warn("Failed to delete Keycloak user {}: {}", keycloakUserId, e.getMessage());
        }
    }
}