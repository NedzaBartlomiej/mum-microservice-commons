package pl.bartlomiej.mummicroservicecommons.authconversion.external.servlet;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import pl.bartlomiej.mummicroservicecommons.authconversion.external.model.UserRoleAuthority;
import pl.bartlomiej.mummicroservicecommons.authconversion.internal.AbstractJwtGrantedAuthoritiesConverter;

import java.util.Collection;
import java.util.stream.Collectors;

public class KeycloakJwtGrantedAuthoritiesConverter extends AbstractJwtGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
    @Override
    public Collection<GrantedAuthority> convert(Jwt source) {
        return super.extractRoles(source).stream()
                .map(UserRoleAuthority::new)
                .collect(Collectors.toList());
    }
}