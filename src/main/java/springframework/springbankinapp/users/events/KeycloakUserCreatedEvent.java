package springframework.springbankinapp.users.events;

import lombok.*;

@AllArgsConstructor
@Getter
public class KeycloakUserCreatedEvent {

    private final String keycloakUserId;

}
