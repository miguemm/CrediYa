package dev.miguel.security.jwt;

import dev.miguel.model.userContext.UserContext;
import dev.miguel.model.userContext.gateways.IExtractUserContext;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class JwtService implements IExtractUserContext {

    @Override
    public UserContext toUserContext(Object p) {
        if (p instanceof JwtAuthenticationToken jat) {
            var jwt   = jat.getToken();
            var id    = jwt.getSubject();
            var email = jwt.getClaimAsString("email");
            var roles = jat.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();
            return new UserContext(id, email, roles);
        }
        if (p instanceof Authentication a) {
            var roles = a.getAuthorities().stream()
                    .map(org.springframework.security.core.GrantedAuthority::getAuthority)
                    .toList();
            return new UserContext(a.getName(), null, roles);
        }
        return new UserContext(null, null, java.util.List.of());
    }
}
