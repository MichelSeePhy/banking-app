package springframework.springbankinapp.users.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.*;
import springframework.springbankinapp.auth.KeycloakService;
import springframework.springbankinapp.users.events.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class KeycloakRollbackListener {

    private final KeycloakService keycloakService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void handleCreationRollback(KeycloakUserCreatedEvent event) {
        log.warn("Rollback detected, deleting Keycloak user: {}", event.getKeycloakUserId());
        keycloakService.deleteUser(event.getKeycloakUserId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void handleUpdateRollback(KeycloakUserUpdatedEvent event) {
        log.warn("Rollback detected, reverting Keycloak user: {}", event.getKeycloakUserId());
        keycloakService.revertUser(
                event.getKeycloakUserId(),
                event.getPreviousEmail(),
                event.getPreviousFirstName(),
                event.getPreviousLastName());
    }

}
