package org.grnet.status.mappers;

import org.grnet.status.dtos.user.UserProfileDto;
import org.grnet.status.entities.User;
import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Mapper(imports = {Timestamp.class, Instant.class})
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @IterableMapping(qualifiedByName = "map")
    List<UserProfileDto> usersToUserProfileDtos(List<User> users);

    @Named("map")
    UserProfileDto userToProfileDto(User user);

    // Conversion helpers
    default Instant map(Timestamp timestamp) {
        return timestamp != null ? timestamp.toInstant() : null;
    }

    default Timestamp map(Instant instant) {
        return instant != null ? Timestamp.from(instant) : null;
    }
}
