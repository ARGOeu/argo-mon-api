package org.grnet.status.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.UriInfo;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import org.grnet.endpoint.scanner.runtime.clients.groupmanagement.AuthGroupManagement;
import org.grnet.endpoint.scanner.runtime.clients.groupmanagement.response.*;
import org.grnet.endpoint.scanner.runtime.entitlements.EntitlementUtils;
import org.grnet.status.dtos.pagination.PageResource;
import org.grnet.status.entities.Page;
import org.grnet.status.entities.PageQueryImpl;
import org.grnet.status.enums.resources.TenantResource;
import org.grnet.status.repositories.TenantRepository;
import org.grnet.status.util.Utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service responsible for managing groups and their members through the external authorization provider.
 */
@ApplicationScoped
public class GroupManagementService {

    @Inject
    AuthGroupManagement groupManagement;

    @Inject
    Utility utility;

    @Inject
    TenantRepository tenantRepository;

    @ConfigProperty(name = "api.auth.entitlements.parent-group")
    String parentGroup;

    @ConfigProperty(name = "api.ui.url")
    String uiBaseUrl;

    @ConfigProperty(name = "api.auth.entitlements.parent-group")
    String namespace;

    @ConfigProperty(name = "api.members-group")
    String membersGroup;

    /**
     * Returns a paginated list of members for the given group.
     *
     * @param groupName the group identifier
     * @param search optional search filter
     * @param page 0-based page index
     * @param size number of records per page
     * @param uriInfo request context for pagination links
     * @return paginated list of group members
     */
    public PageResource<GroupUserResponse> getAllMembers(String groupName, String search, int page, int size, UriInfo uriInfo) {

        var response = getMembers(groupName, page * size, size, search);

        var members = response.results
                .stream()
                .map(groupMember -> mapApplicationMember(groupMember.user))
                .toList();

        var pageable = new PageQueryImpl<GroupUserResponse>();

        pageable.list = members;
        pageable.index = page;
        pageable.size = size;
        pageable.count = response.count;
        pageable.page = Page.of(page, size);

        return new PageResource<>(pageable, uriInfo);
    }


    /**
     * Maps an AGM group user to the application member response model,
     * including tenant roles resolved from local entitlements.
     *
     * @param gu AGM group user
     * @return mapped application member response
     */
    private GroupUserResponse mapApplicationMember(GroupUser gu) {

        var user = new GroupUserResponse();
        user.id = gu.id;
        user.email = gu.email;
        user.username = gu.username;
        user.firstName = gu.firstName;
        user.lastName = gu.lastName;
        user.uid = gu.getUid();
        user.groups = new ArrayList<>();

        if (gu.attributes == null || gu.attributes.getLocalEntitlements() == null) {
            return user;
        }

        var parsedEntitlements = EntitlementUtils.parseEntitlements(
                gu.attributes.getLocalEntitlements()
        );

        var tenantRoles = EntitlementUtils.extractResourceRoles(parsedEntitlements)
                .stream()
                .filter(entitlement ->
                        TenantResource.TENANT.resourceName().equals(entitlement.resource()))
                .map(entitlement -> {
                    var dto = new UserGroupInfoDto();
                    dto.name = resolveResourceName(entitlement.resource(), entitlement.resourceId());
                    dto.role = entitlement.role();
                    return dto;
                })
                .toList();

        user.groups.addAll(tenantRoles);

        return user;
    }


    /**
     * Resolves a resource identifier to a display name when supported.
     *
     * @param resource resource type
     * @param resourceId resource identifier
     * @return resolved resource name or the original identifier
     */
    String resolveResourceName(String resource, String resourceId) {

        if (TenantResource.TENANT.resourceName().equals(resource)) {
            return tenantRepository.findByIdOptional(resourceId)
                    .map(tenant -> tenant.name)
                    .orElse(resourceId);
        }

        return resourceId;
    }

    /**
     * Fetches members of a group directly from the authorization provider.
     *
     * @param groupName the group identifier
     * @param first starting index (offset)
     * @param max maximum number of results
     * @param search optional search filter
     * @return provider response containing members and total count
     */
    public GroupMembersResponse getMembers(String groupName, int first, int max, String search) {

        var fullPath = normalizePath(parentGroup) + "/" + groupName;

        return groupManagement.fetchGroupMembers(fullPath, first, max, search);
    }

    /**
     * Returns all application members assigned to a tenant with the specified role.
     *
     * @param tenantId tenant identifier
     * @param role tenant role (e.g. tenant_admin, tenant_viewer)
     * @return list of users assigned to the tenant role
     */
    public List<GroupUser> getTenantMembersByRole(String tenantId, String role) {

        return getAllApplicationMembers("")
                .stream()
                .filter(user -> hasTenantRole(user, tenantId, role))
                .toList();
    }


    /**
     * Checks whether a user has the specified tenant role
     * based on local entitlement assignments.
     *
     * @param user AGM group user
     * @param tenantId tenant identifier
     * @param role tenant role
     * @return true if the user has the tenant role
     */
    private boolean hasTenantRole(GroupUser user, String tenantId, String role) {

        if (user.attributes == null || user.attributes.getLocalEntitlements() == null) {
            return false;
        }

        var parsedEntitlements = EntitlementUtils.parseEntitlements(
                user.attributes.getLocalEntitlements()
        );

        return EntitlementUtils.extractResourceRoles(parsedEntitlements)
                .stream()
                .filter(entitlement -> TenantResource.TENANT.resourceName().equals(entitlement.resource()))
                .filter(entitlement -> tenantId.equals(entitlement.resourceId()))
                .anyMatch(entitlement -> role.equals(entitlement.role()));
    }


    /**
     * Adds a user to a group with the default role "member".
     *
     * @param groupName the group identifier
     * @param username user identifier recognized by the auth provider
     */
    public void addMember(String groupName, String username) {

        var fullPath = normalizePath(parentGroup) + "/" + groupName;

        groupManagement.addGroupMember(fullPath, username, "member");
    }


    /**
     * Normalizes a group path.
     *
     * @param p raw path
     * @return normalized path
     */
    private static String normalizePath(String p) {
        if (p == null || p.isBlank()) return "";
        p = p.trim();

        // ensure leading slash
        if (!p.startsWith("/")) p = "/" + p;

        // remove trailing slash
        if (p.endsWith("/")) p = p.substring(0, p.length() - 1);

        return p;
    }

    /**
     * Returns a paginated list of groups from the authorization provider.
     *
     * @param page 0-based page index
     * @param size page size
     * @param uriInfo request context for pagination links
     * @return paginated list of groups
     */
    public PageResource<PartialGroup> fetchGroups(int page, int size, UriInfo uriInfo){

        var groups = groupManagement.fetchGroups();

        var partition = utility.partition(new ArrayList<>(groups), size);

        var pageableMembers = partition.get(page) == null ? Collections.EMPTY_LIST : partition.get(page);

        var pageable = new PageQueryImpl<PartialGroup>();

        pageable.list = pageableMembers;
        pageable.index = page;
        pageable.size = size;
        pageable.count = groups.size();
        pageable.page = Page.of(page, size);

        return new PageResource<>(pageable, uriInfo);
    }

    public GroupMembersResponse getApplicationMembers(int first, int max, String search) {

        return getMembers(membersGroup, first, max, search);
    }

    /**
     * Retrieves all application members from AGM using paginated requests.
     *
     * @param search optional search filter
     * @return complete list of application members
     */
    public List<GroupUser> getAllApplicationMembers(String search) {

        int first = 0;
        int size = 100;

        List<GroupUser> users = new ArrayList<>();

        while (true) {
            var response = getApplicationMembers(first, size, search);

            if (response == null || response.results == null || response.results.isEmpty()) {
                break;
            }

            response.results.forEach(member -> users.add(member.user));

            first += size;

            if (users.size() >= response.count) {
                break;
            }
        }

        return users;
    }
}
