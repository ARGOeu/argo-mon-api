package org.grnet.status.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
@Entity
@Table(name = "t_downtime")
@Getter
@Setter
public class Downtime {

    @Id
    @UuidGenerator
    @Column(length = 36)
    private String id;

    @Column(name="tenant_id", nullable = false)
    private String tenant;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String severity;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(nullable = false)
    private String classification;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;


    @OneToMany(
            mappedBy = "downtime",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<DowntimeServiceEndpoint> services = new ArrayList<>();


    @Column(name="created_by", nullable = false)
    private String createdBy;

    @Column(name="updated_by")
    private String updatedBy;


    public Downtime() {
    }


    public void addService(DowntimeServiceEndpoint service) {
        services.add(service);
        service.setDowntime(this);
    }


    public void removeService(DowntimeServiceEndpoint service) {
        services.remove(service);
        service.setDowntime(null);
    }
}