package org.grnet.status.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import java.sql.Timestamp;

@Entity
@Table(name = "t_Tenant")

public class Tenant {

    @Id
    public String id;

    @NotNull
    @Column(name = "name", unique = true)
    public String name;

    @Column(name = "email")
    public String email;

    @Column(name = "website")
    public String website;

    @Column(name = "description")
    public String description;

    @NotNull
    @Column(name = "updated_by")
    public String updatedBy;

    @Column(name = "image")
    public String image;
    @NotNull
    @Column(name = "created_at")
    public Timestamp createdAt;

    @Column(name = "updated_at")
    public Timestamp updatedAt;

}
