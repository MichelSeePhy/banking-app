package springframework.springbankinapp.users.events;

import lombok.*;

@Getter
@AllArgsConstructor
public class KeycloakUserUpdatedEvent {

    private final String keycloakUserId;
    private final String previousEmail;
    private final String previousFirstName;
    private final String previousLastName;

}
