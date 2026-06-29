package org.grnet.status.services;

import jakarta.enterprise.context.ApplicationScoped;
import org.grnet.status.dtos.metadata.MetadataAttributeDto;
import org.grnet.status.dtos.metadata.RoleAssignmentMetadataResponseDto;
import org.grnet.status.dtos.metadata.RoleMetadataResponseDto;
import org.grnet.status.enums.resources.TenantResource;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class RoleMetadataService {

    public RoleMetadataResponseDto getRoleMetadata() {

        var response = new RoleMetadataResponseDto();

        response.attributes = List.of(
                attribute("preferred_name", "Preferred Name", true),
                attribute("description", "Description", false)
        );

        return response;
    }

    public RoleAssignmentMetadataResponseDto getRoleAssignmentMetadata() {

        var response = new RoleAssignmentMetadataResponseDto();

        response.resources = Map.of(
                TenantResource.TENANT.resourceName(), List.of(
                        attribute("preferred_role_name", "Preferred Role Name", true),
                        attribute("tenant_name", "Tenant Name", true),
                        attribute("role_description", "Role Description", false)
                )
        );

        return response;
    }

    private MetadataAttributeDto attribute(String key, String label, boolean required) {

        var dto = new MetadataAttributeDto();
        dto.key = key;
        dto.label = label;
        dto.required = required;

        return dto;
    }
}