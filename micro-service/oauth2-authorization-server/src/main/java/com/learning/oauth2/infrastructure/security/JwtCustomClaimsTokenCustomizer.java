package com.learning.oauth2.infrastructure.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.stereotype.Component;

@Component
public class JwtCustomClaimsTokenCustomizer implements OAuth2TokenCustomizer<JwtEncodingContext> {

    @Override
    public void customize(JwtEncodingContext context) {
        Authentication principal = context.getPrincipal();
        if (!(principal.getPrincipal() instanceof DomainUser domainUser)) {
            return;
        }

        if ("access_token".equals(context.getTokenType().getValue())) {
            context.getClaims().claims(claims -> {
                claims.put("sub", domainUser.getUserId());
                claims.put("email", domainUser.getEmail());
                claims.put("fullName", domainUser.getFullName());
                claims.put("roles", domainUser.getRoles());
                claims.put("permissions", domainUser.getPermissions());
            });
        }
    }
}
