package org.grnet.status.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;


import java.sql.Timestamp;

@Entity
@Table(name = "t_Status_Page")
public class StatusPage {

    @Id
    @UuidGenerator
    public String id;

    @NotNull
    @Column(name = "name")
    public String name;

    @NotNull
    @Column(name = "slug")
    public String slug;

    @NotNull
    @Column(name = "user_id")
    public String userId;

    @NotNull
    @Column(name = "api")
    public String api;

    @NotNull
    @Column(name = "secret")
    public String secret;

    @NotNull
    @Column
    public String report;

    @Column(name = "config", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    public String config;

    @Column(name = "created_at")
    public Timestamp createdAt;

    @Column(name = "updated_at")
    public Timestamp updatedAt;

}
