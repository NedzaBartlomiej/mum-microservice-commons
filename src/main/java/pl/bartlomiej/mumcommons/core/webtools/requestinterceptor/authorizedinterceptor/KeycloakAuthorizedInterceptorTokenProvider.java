package pl.bartlomiej.mumcommons.core.webtools.requestinterceptor.authorizedinterceptor;

import org.keycloak.admin.client.token.TokenManager;

public class KeycloakAuthorizedInterceptorTokenProvider implements AuthorizedInterceptorTokenProvider {

    private final TokenManager tokenManager;

    public KeycloakAuthorizedInterceptorTokenProvider(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    @Override
    public String getValidToken() {
        return tokenManager.getAccessTokenString();
    }
}
