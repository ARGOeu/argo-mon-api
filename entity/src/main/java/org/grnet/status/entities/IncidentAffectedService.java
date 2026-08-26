package org.grnet.status.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "t_Incident_Service")
@Getter
@Setter
public class IncidentAffectedService {

    @Id
    @UuidGenerator
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "incident_id")
    @NotNull
    private Incident incident;

    @NotBlank
    @Column(name = "service_id")
    private String serviceId;

    @NotBlank
    @Column(name = "service_name")
    private String serviceName;
}