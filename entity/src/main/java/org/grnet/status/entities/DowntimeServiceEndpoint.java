package org.grnet.status.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "t_downtime_service_endpoint")
@Getter
@Setter
public class DowntimeServiceEndpoint {

    @Id
    @UuidGenerator
    @Column(length = 36)
    private String id;

    @Column(nullable = false)
    private String hostname;

    @Column(nullable = false)
    private String service;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "downtime_id",
            nullable = false
    )
    private Downtime downtime;

}
