package com.learning.user.infrastructure.mapper;

import com.learning.user.domain.User;
import com.learning.user.domain.UserStatus;
import com.learning.user.infrastructure.persistence.UserDocument;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-10-16T14:25:35+0530",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.7 (Homebrew)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public UserDocument toDocument(User user) {
        if ( user == null ) {
            return null;
        }

        UserDocument.UserDocumentBuilder userDocument = UserDocument.builder();

        userDocument.id( user.getId() );
        userDocument.email( user.getEmail() );
        userDocument.passwordHash( user.getPasswordHash() );
        userDocument.fullName( user.getFullName() );
        Set<String> set = user.getRoles();
        if ( set != null ) {
            userDocument.roles( new LinkedHashSet<String>( set ) );
        }
        userDocument.status( user.getStatus() );
        userDocument.createdAt( user.getCreatedAt() );
        userDocument.updatedAt( user.getUpdatedAt() );

        return userDocument.build();
    }

    @Override
    public User toDomain(UserDocument userDocument) {
        if ( userDocument == null ) {
            return null;
        }

        Set<String> roles = null;
        String id = null;
        String email = null;
        String passwordHash = null;
        String fullName = null;
        UserStatus status = null;
        Instant createdAt = null;
        Instant updatedAt = null;

        Set<String> set = userDocument.getRoles();
        if ( set != null ) {
            roles = new LinkedHashSet<String>( set );
        }
        id = userDocument.getId();
        email = userDocument.getEmail();
        passwordHash = userDocument.getPasswordHash();
        fullName = userDocument.getFullName();
        status = userDocument.getStatus();
        createdAt = userDocument.getCreatedAt();
        updatedAt = userDocument.getUpdatedAt();

        User user = new User( id, email, passwordHash, fullName, roles, status, createdAt, updatedAt );

        return user;
    }
}
