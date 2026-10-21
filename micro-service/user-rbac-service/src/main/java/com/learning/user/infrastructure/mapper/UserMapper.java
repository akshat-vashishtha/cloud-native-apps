package com.learning.user.infrastructure.mapper;

import com.learning.user.domain.User;
import com.learning.user.infrastructure.persistence.UserDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper (componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "version", ignore = true)
    UserDocument toDocument(User user);

    User toDomain(UserDocument userDocument);
}
