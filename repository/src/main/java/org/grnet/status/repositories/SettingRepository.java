package org.grnet.status.repositories;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.grnet.status.entities.Setting;

import java.util.Optional;

@ApplicationScoped
public class SettingRepository implements Repository<Setting, String>{

    @Inject
    EntityManager entityManager;

    public Optional<Setting> findPerformanceSetting() {
        return entityManager.createNativeQuery("""
                SELECT *
                FROM t_Setting
                WHERE setting_data ->> 'label' = :label
                """, Setting.class)
                .setParameter("label", "Performance Monitoring")
                .getResultStream()
                .findFirst();
    }
}
