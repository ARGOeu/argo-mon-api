package org.grnet.status.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.grnet.status.enums.IncidentStatus;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "t_Incident")
@Getter
@Setter
public class Incident {

    @Id
    @UuidGenerator
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    @NotNull
    private Tenant tenant;

    @NotBlank
    @Column(name = "title")
    private String title;

    @NotBlank
    @Column(name = "description")
    private String description;

    @NotBlank
    @Column(name = "service_id")
    private String serviceId;

    @NotBlank
    @Column(name = "service_name")
    private String serviceName;

    @NotBlank
    @Column(name = "incident_number")
    private String incidentNumber;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private IncidentStatus status = IncidentStatus.REPORTED;

    @NotBlank
    @Column(name = "created_by")
    private String createdBy;

    @NotNull
    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_by")
    private String updatedBy;


    @NotNull
    @Column(name = "updated_at")
    private Instant updatedAt;

    @OneToMany(
            mappedBy = "incident",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<IncidentComment> comments = new ArrayList<>();

    @PrePersist
    private void prePersist() {
        var now = Instant.now();

        if (status == null) {
            status = IncidentStatus.REPORTED;
        }

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    private void preUpdate() {
        updatedAt = Instant.now();
    }
}