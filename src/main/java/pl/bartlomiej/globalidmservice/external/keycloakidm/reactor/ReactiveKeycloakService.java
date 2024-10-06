package pl.bartlomiej.globalidmservice.external.keycloakidm.reactor;

import pl.bartlomiej.globalidmservice.external.keycloakidm.model.KeycloakRole;
import pl.bartlomiej.globalidmservice.external.keycloakidm.model.KeycloakUserRegistration;
import pl.bartlomiej.globalidmservice.external.keycloakidm.model.KeycloakUserRepresentation;
import reactor.core.publisher.Mono;

public interface ReactiveKeycloakService {
    Mono<KeycloakUserRepresentation> create(KeycloakUserRegistration keycloakUserRegistration);

    Mono<Void> delete(String id);

    Mono<Void> assignRole(String id, KeycloakRole keycloakRole);
}