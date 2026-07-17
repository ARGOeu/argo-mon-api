package org.grnet.status.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;

@Entity
@Table(name = "t_Incident_Comment")
@Getter
@Setter
public class IncidentComment {

    @Id
    @UuidGenerator
    @Column
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id")
    @NotNull
    private Incident incident;

    @NotBlank
    @Column(name = "comment")
    private String comment;

    @NotBlank
    @Column(name = "created_by")
    private String createdBy;

    @NotNull
    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    private void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}