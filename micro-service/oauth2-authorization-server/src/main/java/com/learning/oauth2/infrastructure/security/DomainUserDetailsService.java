package com.learning.oauth2.infrastructure.security;

import com.learning.oauth2.infrastructure.client.UserAuthDetailsResponse;
import com.learning.oauth2.infrastructure.client.UserServiceClient;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DomainUserDetailsService implements UserDetailsService {

    private final UserServiceClient userServiceClient;

    public DomainUserDetailsService(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserAuthDetailsResponse details = userServiceClient.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        boolean active = details.status() == null || "ACTIVE".equalsIgnoreCase(details.status());

        return new DomainUser(
                details.userId(),
                details.email(),
                details.fullName(),
                details.passwordHash(),
                details.roles(),
                details.permissions(),
                active
        );
    }
}
