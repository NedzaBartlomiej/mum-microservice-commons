package pl.bartlomiej.mummicroservicecommons.globalidmservice.internal.keycloakidm;

import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponseException;
import pl.bartlomiej.mummicroservicecommons.globalidmservice.external.keycloakidm.model.KeycloakRole;
import pl.bartlomiej.mummicroservicecommons.globalidmservice.external.keycloakidm.model.KeycloakUserRegistration;
import pl.bartlomiej.mummicroservicecommons.globalidmservice.external.keycloakidm.model.KeycloakUserRepresentation;
import pl.bartlomiej.mummicroservicecommons.globalidmservice.external.keycloakidm.servlet.KeycloakService;
import pl.bartlomiej.mummicroservicecommons.offsettransaction.servlet.OffsetTransactionOperator;

import java.util.Collections;

class DefaultKeycloakService extends AbstractKeycloakService implements KeycloakService {

    private static final Logger log = LoggerFactory.getLogger(DefaultKeycloakService.class);
    private final RealmResource realmResource;

    DefaultKeycloakService(KeycloakIDMServiceProperties properties, Keycloak keycloak) {
        this.realmResource = keycloak.realm(properties.realmName());
    }

    @Override
    public KeycloakUserRepresentation create(final KeycloakUserRegistration keycloakUserRegistration) {
        log.info("Started keycloak user creation process.");

        UserRepresentation userRepresentation = AbstractKeycloakService.buildUserRepresentation(keycloakUserRegistration);

        UsersResource usersResource = this.realmResource.users();

        log.info("Creating user.");
        KeycloakUserRepresentation createdUser;
        try (Response response = usersResource.create(userRepresentation)) {
            handleResponseStatus(response, HttpStatus.CREATED);

            createdUser = new KeycloakUserRepresentation(
                    CreatedResponseUtil.getCreatedId(response),
                    keycloakUserRegistration.getUsername(),
                    keycloakUserRegistration.getEmail()
            );
        }

        OffsetTransactionOperator.performOffsetConsumerTransaction(
                createdUser,
                createdUser.id(),
                u -> this.assignRole(u.id(), keycloakUserRegistration.getDefaultRole()),
                this::delete
        );

        log.info("Sending verification email message.");
        OffsetTransactionOperator.performOffsetConsumerTransaction(
                createdUser,
                createdUser.id(),
                u -> usersResource.get(u.id()).sendVerifyEmail(),
                this::delete
        );

        log.debug("Returning successfully created user.");
        return createdUser;
    }

    @Override
    public void delete(final String id) {
        log.info("Started deletion keycloak user process.");
        try (Response response = realmResource.users().delete(id)) {
            handleResponseStatus(response, HttpStatus.NO_CONTENT);
        }
        log.info("User has been deleted from the keycloak auth-server.");
    }

    @Override
    public void assignRole(final String id, final KeycloakRole keycloakRole) {
        log.info("Started user assigning role process.");
        UserResource userResource = realmResource.users().get(id);
        RolesResource rolesResource = realmResource.roles();

        log.debug("Getting equivalent keycloak role to argument role.");
        RoleRepresentation roleRepresentation = rolesResource.get(keycloakRole.getRole()).toRepresentation();

        log.info("Assigning keycloak role to an user.");
        userResource.roles().realmLevel().add(Collections.singletonList(roleRepresentation));
    }

    private static void handleResponseStatus(final Response response, final HttpStatus successStatus) {
        if (response.getStatus() != successStatus.value()) {
            log.error("Some error status occurred in keycloak user creation process response: {}", response.getStatusInfo());
            throw new ErrorResponseException(HttpStatus.valueOf(response.getStatus()));
        }
    }
}