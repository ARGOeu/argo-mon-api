package org.grnet.status.repositories;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.grnet.status.entities.PageQuery;
import org.grnet.status.entities.User;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

@ApplicationScoped
public class UserRepository implements UserRepositoryI<User, String> {

    /**
     * It executes a query in database to retrieve user's profile.
     *
     * @param id User's Unique identifier (voperson_id)
     * @return User's Profile.
     */
    @Override
    public User fetchUser(String id) {

        return find("from User user where user.id = ?1", id).firstResult();
    }

    @Override
    public Optional<User> fetchActiveUserByEmail(String email) {
        return Optional.empty();
    }

    @Override
    public Optional<User> fetchUserByEmail(String email) {
        return Optional.empty();
    }

    @Override
    @Transactional
    public User updateUserMetadata(String id, String name, String surname, String email) {

        var user = findById(id);

        if (user == null) {
            throw new EntityNotFoundException("User not found with id: " + id);
        }

        user.setName(name);
        user.setSurname(surname);
        user.setEmail(email);

        return user;
    }

    @Override
    public PageQuery<User> fetchUsersByPage(String search, String sort, String order, String status, String type, int page, int size) {
        return null;
    }

}
