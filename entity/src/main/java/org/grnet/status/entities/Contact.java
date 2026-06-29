package org.grnet.status.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.grnet.status.entities.conventer.ContactTypeConverter;
import org.grnet.status.enums.ContactType;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(
        name = "t_Contact",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"contact_name", "contact_email", "contact_type"}
        )
)
@Getter
@Setter
public class Contact {
    @Id
    @UuidGenerator
    private String id;

    @NotNull
    @Column(name = "contact_name")
    private String contactName;

    @NotNull
    @Column(name = "contact_email")
    private String contactEmail;


    @Convert(converter = ContactTypeConverter.class)
    @NotNull
    @Column(name = "contact_type")
    private ContactType contactType;

    @ManyToMany(mappedBy = "contacts")
    private java.util.Set<Tenant> tenants = new java.util.HashSet<>();

    // getters and setters
    public boolean equalsByInfo(Contact other) {
        return this.contactName.equals(other.contactName) && this.contactEmail.equals(other.contactEmail) && this.contactType.name().equals(other.contactType.name());
    }
}