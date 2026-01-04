package springframework.springbankinapp.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.*;
import org.springframework.context.annotation.*;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class KeycloakConfig {

    private final KeycloakProperties keycloakProperties;

    @Bean
    public Keycloak keycloak() {
        Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(keycloakProperties.authServerUrl())
                .realm(keycloakProperties.realm())
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(keycloakProperties.clientId())
                .clientSecret(keycloakProperties.clientSecret())
                .build();

        log.info("Keycloak client configured: serverUrl={}, realm={}, clientId={}",
                keycloakProperties.authServerUrl(), keycloakProperties.realm(), keycloakProperties.clientId());

        return keycloak;
    }
}