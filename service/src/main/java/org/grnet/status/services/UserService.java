package org.grnet.status.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.grnet.status.dtos.user.UpdateUserProfileDto;
import org.grnet.status.dtos.user.UserProfileDto;
import org.grnet.status.entities.User;
import org.grnet.status.exceptions.ConflictException;
import org.grnet.status.mappers.UserMapper;
import org.grnet.status.repositories.UserRepository;
import org.grnet.status.util.Utility;

@ApplicationScoped
public class UserService {


    @Inject
    UserRepository userRepository;

    @Inject
    Utility utility;


    @Transactional
    public UserProfileDto getUserProfile(String id) {

        var userProfile = userRepository.fetchUser(id);

        if (userProfile == null) {
            throw new EntityNotFoundException("User with id '" + id + "' is not registered.");
        }

        return UserMapper.INSTANCE.userToProfileDto(userProfile);
    }

    public UserProfileDto updateUserProfile(String id, UpdateUserProfileDto request) {

        var optionalUser = userRepository.fetchUserByEmail(request.email);

        if (optionalUser.isPresent() && !optionalUser.get().getId().equals(id)){
            throw new ConflictException("There is a User with email : " + request.email);
        }
        var user = userRepository.updateUserMetadata(id, request.name, request.surname, request.email);

        return UserMapper.INSTANCE.userToProfileDto(user);
    }


    @Transactional
    public UserProfileDto registerUser() {

        var userId = utility.getUserUniqueIdentifier();

        var optionalUser = userRepository.searchByIdOptional(userId);
        if (optionalUser.isPresent()) {
            throw new ConflictException("User already exists with id: " + userId);
        }

        var newUser = new User();
        newUser.setId(userId);
        newUser.setUsername(utility.getUsername());
        newUser.setEmail(utility.getUserEmail());
        newUser.setName(utility.getUserName());
        newUser.setSurname(utility.getUserSurname());

        userRepository.persist(newUser);
        return UserMapper.INSTANCE.userToProfileDto(newUser);
    }

    @Transactional
    public void deleteAll() {
        userRepository.deleteAll();
    }

}
