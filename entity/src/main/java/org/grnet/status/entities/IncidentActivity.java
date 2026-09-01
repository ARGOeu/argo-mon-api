package org.grnet.status.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.grnet.status.enums.IncidentStatus;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;

@Entity
@Table(name = "t_Incident_Activity")
@Getter
@Setter
public class IncidentActivity {

    @Id
    @UuidGenerator
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id")
    @NotNull
    private Incident incident;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status")
    private IncidentStatus previousStatus;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "new_status")
    private IncidentStatus newStatus;

    @Column(name = "status_description")
    private String statusDescription;

    @NotNull
    @Column(name = "changed_by")
    private String changedBy;

    @NotNull
    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    private void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }

        if (statusDescription == null || statusDescription.isBlank()) {
            statusDescription = "Status changed to " + newStatus;
        }

    }
}