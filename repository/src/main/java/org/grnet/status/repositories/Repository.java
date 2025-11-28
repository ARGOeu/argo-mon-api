package org.grnet.status.repositories;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import java.util.Optional;

/**
 * Base repository interface with common CRUD operations for all entities.
 *
 * @param <E>  Entity type
 * @param <ID> ID type
 */
public interface Repository<E, ID> extends PanacheRepositoryBase<E, ID> {

    default Optional<E> searchByIdOptional(ID id){
        return findByIdOptional(id);
    }

    default boolean existsByField(String fieldName, Object value) {
        return find(fieldName + " = ?1", value)
                .firstResultOptional()
                .isPresent();
    }
}
