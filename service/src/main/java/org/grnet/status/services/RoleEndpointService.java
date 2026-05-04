package org.grnet.status.services;


import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.grnet.endpoint.scanner.runtime.entities.RoleEndpoint;
import org.grnet.endpoint.scanner.runtime.entities.RoleEndpointRepository;
import org.grnet.status.dtos.RoleEndpointAssignmentResponse;
import org.grnet.status.dtos.role.RoleEndpointAssignmentRequest;

import javax.management.relation.Role;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class RoleEndpointService {

    @Inject
    RoleEndpointRepository roleEndpointRepository;

    public RoleEndpointAssignmentResponse getAssignedEndpointsPerRole() {

        List<RoleEndpoint> roleEndpoints = roleEndpointRepository.findAll();

        Map<String, List<RoleEndpoint>> grouped =
                roleEndpoints.stream()
                        .collect(Collectors.groupingBy(RoleEndpoint::getRoleId));

        List<RoleEndpointAssignmentResponse.RoleAssignment> assignments =
                grouped.entrySet().stream()
                        .map(entry -> {
                            List<RoleEndpoint> group = entry.getValue();
                            RoleEndpoint first = group.get(0);

                            RoleEndpointAssignmentResponse.RoleAssignment ra =
                                    new RoleEndpointAssignmentResponse.RoleAssignment();

                            ra.setRoleId(entry.getKey()); // ✅ safer than first.getRoleId()
                            ra.setRoleName(first.getRoleName());

                            ra.setSecuredEndpointIds(
                                    group.stream()
                                            .map(RoleEndpoint::getSecuredEndpointId)
                                            .distinct()
                                            .collect(Collectors.toList())
                            );

                            return ra;
                        })
                        .collect(Collectors.toList());

        RoleEndpointAssignmentResponse response = new RoleEndpointAssignmentResponse();
        response.setAssignments(assignments);

        return response;
    }

    @Transactional
    public void assignRolesToEndpoints(RoleEndpointAssignmentRequest request) {

        List<RoleEndpoint> existing = roleEndpointRepository.findAll();

        Set<String> requestedRoleIds = request.getAssignments()
                .stream()
                .map(RoleEndpointAssignmentRequest.RoleAssignment::getRoleId)
                .collect(Collectors.toSet());

        Set<String> dbRoleIds = existing.stream()
                .map(RoleEndpoint::getRoleId)
                .collect(Collectors.toSet());

        // DELETE roles not in request
        Set<String> rolesToDelete = new HashSet<>(dbRoleIds);
        rolesToDelete.removeAll(requestedRoleIds);

        for (String roleId : rolesToDelete) {
            roleEndpointRepository.deleteByRoleId(roleId);
        }

        //  SYNC remaining roles
        for (var assignment : request.getAssignments()) {

            String roleId = assignment.getRoleId();
            String roleName = assignment.getRoleName();

            Set<String> incoming = new HashSet<>(assignment.getSecuredEndpointIds());

            List<RoleEndpoint> roleEntries =
                    roleEndpointRepository.list("role_id", roleId);

            Set<String> existingEndpoints = roleEntries.stream()
                    .map(RoleEndpoint::getSecuredEndpointId)
                    .collect(Collectors.toSet());

            // delete removed endpoints
            Set<String> toDelete = new HashSet<>(existingEndpoints);
            toDelete.removeAll(incoming);

            if (!toDelete.isEmpty()) {
                roleEndpointRepository.deleteByRoleIdAndEndpointIds(roleId, new ArrayList<>(toDelete));
            }

            // insert new endpoints
            Set<String> toInsert = new HashSet<>(incoming);
            toInsert.removeAll(existingEndpoints);

            for (String endpointId : toInsert) {
                RoleEndpoint re = new RoleEndpoint();
                re.setRoleId(roleId);
                re.setRoleName(roleName);
                re.setSecuredEndpointId(endpointId);
                roleEndpointRepository.create(re);
            }
        }
    }
}
