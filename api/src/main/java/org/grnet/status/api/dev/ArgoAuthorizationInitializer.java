package org.grnet.status.api.dev;

import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.grnet.endpoint.scanner.runtime.Scope;
import org.grnet.endpoint.scanner.runtime.dtos.AssignRoleRequest;
import org.grnet.endpoint.scanner.runtime.dtos.CreateRoleRequest;
import org.grnet.endpoint.scanner.runtime.dtos.SecuredEndpointAssignment;
import org.grnet.endpoint.scanner.runtime.dtos.SecuredEndpointPerRoleRequest;
import org.grnet.endpoint.scanner.runtime.services.ResourceAuthorizationService;
import org.grnet.endpoint.scanner.runtime.services.RoleEndpointService;
import java.util.List;
import java.util.Map;

@ApplicationScoped
@IfBuildProfile("dev")
public class ArgoAuthorizationInitializer {

    @Inject ResourceAuthorizationService resourceAuthorizationService;
    @Inject RoleEndpointService roleEndpointService;

    void onStart(@Observes StartupEvent event) {
        Log.info("Initializing ARGO Monitoring authorization (dev mode)...");
        try {
            initializeRole("members", "Members", "Registered users of ARGO Monitoring.", membersEndpoints());
            initializeRole("tenant_admin", "Tenant Admin", "Administrators who manage tenants and their associated resources.", tenant_adminEndpoints());
            initializeRole("tenant_viewer", "Tenant Viewer", "Users with read-only access to tenant resources.", tenant_viewerEndpoints());
            initializeRole("automation", "Automation", "Automated services that manage and report tenant lifecycle status events.", automationEndpoints());

            // Development tenant IDs
            var tenantTestId = "42c1152d-e23c-4a19-b51a-b27f1eb7f37f";
            var tenantBId = "6b36d6d3-56a3-48a5-93af-aecf3e16a7c6";

            // Global members
            assignRole("user1", "members", null);
            assignRole("user2", "members", null);
            assignRole("user3", "members", null);

            // TENANT-TEST
            assignRole("user1", "tenant_admin", tenantTestId);
            assignRole("user2", "tenant_viewer", tenantTestId);

            // TENANTB
            assignRole("user3", "tenant_admin", tenantBId);

            Log.info("ARGO Monitoring authorization initialized.");

        } catch (Exception e) {
            Log.error("Failed to initialize ARGO Monitoring authorization.", e);
            throw new IllegalStateException("Cannot initialize ARGO authorization", e);
        }
    }

    private void initializeRole(String roleName, String preferredName, String description,
                                List<SecuredEndpointAssignment> endpoints) {
        var role = resourceAuthorizationService.getAllRoles().stream()
                .filter(existing -> roleName.equals(existing.name))
                .findFirst()
                .orElseGet(() -> {
                    var request = new CreateRoleRequest();
                    request.name = roleName;
                    request.attributes = Map.of(
                            "preferred_name", List.of(preferredName),
                            "description", List.of(description)
                    );
                    resourceAuthorizationService.createNewRole(request);
                    return resourceAuthorizationService.getAllRoles().stream()
                            .filter(created -> roleName.equals(created.name))
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException("Role not found: " + roleName));
                });

        var endpointRequest = new SecuredEndpointPerRoleRequest();
        endpointRequest.setAssignments(endpoints);
        roleEndpointService.assignRolesToEndpointsPerRole(role.id, endpointRequest);
        Log.infof("Initialized role %s (id=%s) with %d endpoint assignments",
                roleName, role.id, endpoints.size());
    }

    private static SecuredEndpointAssignment endpoint(String id, Scope scope) {
        var assignment = new SecuredEndpointAssignment();
        assignment.setSecuredEndpointId(id);
        assignment.setScope(scope);
        return assignment;
    }

    private static List<SecuredEndpointAssignment> membersEndpoints() {
        return List.of(
                // GET /v1/users/profile
                endpoint("b32ecfacefb21f938fdbb135d02aaaf2782a99558868a87e348157a4788b40e9", null),
                // PATCH /v1/users/invitations/{id}
                endpoint("30af66a4a5345077ba6b55ade0ccb2bd9fc5d75f705a0dfa2862bd050a3fb009", null),
                // POST /v1/encrypt
                endpoint("f34641f7515ec0f99198b28f2787c722be07b89c9dc7f8224697b4882c6b3afc", null)
        );
    }

    private static List<SecuredEndpointAssignment> tenant_adminEndpoints() {
        return List.of(
                // GET /v1/tenants/{id}/check-readiness
                endpoint("f68ccd41d244c567fb6d308ba3c511007e9679d9b23b6dce6387417b23c29331", null),
                // GET /v1/tenants/{id}/reports
                endpoint("317bf647ef2e8ebc1ddc18e918e8f50e317218b8fbb0c44de13c5b6bf0f7e8cb", null),
                // PATCH /v1/tenants/{id}/incidents/{incident-id}/status
                endpoint("e17ec4bfed6b2cb1a1838a2dd3e78180ac62c821881bd6b5ce451b82bfdc9526", Scope.ALL),
                // GET /v1/tenants/{id}/status/{report-name}/groups
                endpoint("94dfc267a2381a7160d2bc3cd1e5f51a74366ceb3449ca430a73eb0ad289d2d0", null),
                // DELETE /v1/tenants/{id}/topology/service-types
                endpoint("180aeba8c3a84bcf923541360d30db15fba2ebb7f2016edea86e6f5285449115", null),
                // GET /v1/tenants/{id}/aggregation-profiles/{profile_id}
                endpoint("7cff1e350473a2cac2c2a1a2b8d320dce285fe1e9eea4baf24326e86d26d6ba6", null),
                // GET /v1/tenants/{id}/metric-profiles
                endpoint("4bcf5992275fe9c7a23293ca186f0f115c9a71d23dd9dac7c3183a98ba9c01d8", null),
                // PUT /v1/tenants/{id}
                endpoint("8bdfcb7f1256ab1710b86b4bad1260882e9943c4348e23e6e7e765495d99f9d5", null),
                // PUT /v1/tenants/{id}/pages/{page-id}
                endpoint("2535b1dd08457ce59a0ded9c08343c77340454eaacda627f9293a60dc6e13dad", null),
                // PATCH /v1/tenants/{id}/invitations/{invitation_id}
                endpoint("af39c9378a2c0d445061184dccaa20126484eaf0249e770e5357f3f802dc597a", null),
                // GET /v1/tenants/{id}/invitations
                endpoint("39f8988b78ef403c4a75b0449241ea35bfa1f29cf9cb63dcc9b0e782ea32c10c", null),
                // GET /v1/tenants/{id}/results/{report-name}/groups/{group-name}
                endpoint("330b6f873d8b06c0234cc84b64694e67a0a58a0a604b7dd3fc9e0deed03a0049", null),
                // GET /v1/tenants/{id}/status/{report-name}/groups/{group-name}/service-types/{service-type-name}
                endpoint("4a16bb1bda9fa528a2911519402d60a4194899e0b727a28a94c3bf900321bfed", null),
                // GET /v1/tenants/{id}/status
                endpoint("b5d1546f5c5fff86413164d51054cca1f713c3d60a79e114b6ad5569a8bc1ba0", null),
                // GET /v1/tenants/{id}/capabilities/availability
                endpoint("c627d1f9330b3599c8bbec3e2750539b1568261a8ee09441db67c3459d7b96b5", null),
                // GET /v1/tenants/{id}/members
                endpoint("7dbe3cdb0a1e31a897634256adf8363f86beb1a06b01d8f0e12a14bb9383b1f7", null),
                // GET /v1/tenants/{id}/status/{report-name}/groups/{group-name}/endpoints/{endpoint-name}
                endpoint("4269b90b20f48029112a0c3e2aa8ea94f2845f27f9428474cdf8f9a83670922a", null),
                // POST /v1/tenants/{id}/pages
                endpoint("c9ba70db8b9fd1175f83e491dbee0a1c7c842e7ab9329ecdf455e5f0c9b216c9", null),
                // GET /v1/tenants/{id}/incidents/{incident-id}
                endpoint("88d498c6032fb0c79e09dbfbf200d534fb60361b18b518633f802eba0935724f", null),
                // GET /v1/tenants/contact-types
                endpoint("5952b8e656d0e9e870f9b4411ca7188e5076f902d246a2225ceeeab407e12105", null),
                // PUT /v1/tenants/{id}/downtimes/{downtime-id}
                endpoint("a1705e80332a374f066f630002d781eb4eaecb885d1bb26ee6f74c11bb473147", null),
                // GET /v1/tenants/{id}/topology/endpoints
                endpoint("96218affb24c27ddce65f240a911db4f4c6416def4ad822ae445de300c26982d", null),
                // GET /v1/tenants/{id}/status/{report-name}/groups/{group-name}
                endpoint("ca291521f9b414c45c7ca3c6e5117178d77c0ac02170ef344fdebebc759251c4", null),
                // GET /v1/tenants/{id}/incidents/{incident-id}/activity
                endpoint("e2bf37bb28788c2a15f5788484288eb1db65c161365a5a8bc21cffa8de4ccf12", null),
                // GET /v1/tenants/{id}/feeds/topology
                endpoint("37c8ab347ce395ffebe49e7d871ae858481b9d6d6a4d82f4a6e415bcf992cd02", null),
                // GET /v1/tenants/{id}/capabilities/summary/{item}
                endpoint("54a76eb1bbb30d947ea98249fd11bfd840ba2b556f3f2c5eaacf5377e75ca81e", null),
                // GET /roles/{id}/assigned-endpoints
                endpoint("b3298a6d373582e7d592f136830c1f21ba5a174c62ce47498de0dbfd110980f1", null),
                // DELETE /v1/tenants/{id}/topology/endpoints
                endpoint("937c5481a95fd9e0b62350f9b3fadacd6bfda11377d0953d25adf8ec16ccf7cd", null),
                // GET /v1/tenants/{id}/status/{report-name}/groups/{group-name}/endpoints/{endpoint-name}/metrics/{metric-name}
                endpoint("b920b0918309e77897c93cacb9b5ccbd800ff52e987d4287593ac261ca57a501", null),
                // POST /v1/tenants/{id}/reports/{report-id}/set-public
                endpoint("24f4f287220fea01b9fc5122092c378f13eacb386eb5e8c9ae1d767050e8654e", null),
                // GET /v1/tenants/{id}/topology/service-types
                endpoint("55adfea0bd473c93e8a4f48ef5a1508d0d54793d0f693e05540f31417a49a030", null),
                // GET /v1/tenants/{id}/status/{report-name}/groups/{group-name}/endpoints
                endpoint("05fa910be90025c9623da020df59ac8836272d072e9fb2c7e29a28cdcbbf35fd", null),
                // GET /v1/tenants/{id}/status/{report-name}/endpoints/{endpoint-name}
                endpoint("e0bb3d931d05541a390bd5f230a612d1f63336990cc6bd61e84a96f153f62474", null),
                // POST /v1/tenants/{id}/reports/{report-id}/set-node-report
                endpoint("7d7932baf11b80167a0b165ef215ff20a7a3d4ec8f72d69037f269a5684c0933", null),
                // DELETE /roles/revoke
                endpoint("d78ab4365c1072d5ef9e710598a97bc3a4947d18e8031789a31ff8bb79ced870", null),
                // GET /v1/tenants/{id}/results/{report-name}/groups
                endpoint("244bc82fc913ea5aef9aaff212377593bee5a5ce090723ba21df387cfadac5da", null),
                // GET /v1/tenants/{id}/pages
                endpoint("e7757f768a4e07ec2a539b49ba3603733129de77fd133613c545ea3e4de5d4c9", Scope.ALL),
                // GET /v1/tenants/{id}/capabilities/monitoring/metrics
                endpoint("35b25c3fcf450d6b4a305aa730d99a93a55873cd1ee2a72e32722c3ce561c6a9", null),
                // GET /v1/tenants
                endpoint("a0adaa95af8ad1221b569ce2f35ae102f5c8ec4050828408b8258d2adfa2158a", null),
                // POST /v1/tenants/{id}/downtimes
                endpoint("6d3fbc65acc7abc8faff46ded4879ebfbabc61b95ba1cf3ea74ae0e15a9b73fd", null),
                // GET /v1/tenants/{id}/incidents
                endpoint("d2308a024c55a10f012bf6a69b002c92e7d93c6331dc10d7966157a97b1455e6", null),
                // POST /v1/tenants/{id}/reports/{report-id}/set-private
                endpoint("81e4a0243791ee727f02eedfadee0353b7ded91bdfd06addf9a2da2594faec5f", null),
                // POST /v1/tenants/{id}/topology/endpoints
                endpoint("5c890a5eb97702d4857d2d843086e01fb88d136e6087405e4410537ba44aa181", null),
                // GET /v1/tenants/{id}/status/{report-name}/groups/{group-name}/service-types/{service-type-name}/endpoints/{endpoint-name}/metrics
                endpoint("ec8361414eb216a1e584e0b7d7aac31c525b9bc410ab5d1042c2a3bfd37da768", null),
                // GET /v1/tenants/{id}/capabilities/monitoring/metrics/{service-id}
                endpoint("d977748a1f2904b07421d2c3b7aba2bf82492e6ed6b2a793f9b7b5805a1d5c64", null),
                // GET /v1/tenants/{id}/pages/check-slug/{slug}
                endpoint("d45036221162673528396c757339c422be4f59339434f4a651a66c29bdee391e", null),
                // POST /v1/tenants/{id}/topology/service-types
                endpoint("9657b8d8405265142d50276d7796d1e25f30441341ab1478c8db1f89644d5d72", null),
                // GET /v1/tenants/{id}/results/{report-name}/endpoints
                endpoint("86f51239a7598e447c106fed305e138d0b5df197565d2d709478413916185a53", null),
                // GET /v1/tenants/{id}/results/{report-name}/groups/{group-name}/endpoints/{endpoint-name}
                endpoint("c0931d2814ff95b99afa4d16d307c22da6832577d00d0402aa98863e34dc032e", null),
                // GET /v1/tenants/{id}/status/{report-name}/groups/{group-name}/service-types
                endpoint("cadf417f02c6f0ac824b4d00c1324e48a9ceaeb39f60344722ed82260192695b", null),
                // POST /v1/tenants/{id}/notify-ams-check-readiness
                endpoint("5567a34de2c8522ed42bd71d7e5281620b08c485d004ebc384a32dcf5ba329e7", null),
                // GET /v1/tenants/{id}/results/groups
                endpoint("7372467ae57ba2c1d3a10971b807b89e0eed02f524c8bb356fee82c810323afc", null),
                // GET /v1/tenants/{id}/status/{report-name}/groups/{group-name}/service-types/{service-type-name}/endpoints/{endpoint-name}
                endpoint("3c2d447b06c11f35e1bdc16dfe245495bcdec40879ba475de1636600f65e6602", null),
                // GET /v1/tenants/{id}/status/{report-name}/groups/{group-name}/service-types/{service-type-name}/endpoints/{endpoint-name}/metrics/{metric-name}
                endpoint("fa8246ff978d19a26591cfe4d429c0e34928e6f28098f2262ac130e3f8ab1fd6", null),
                // GET /v1/tenants/{id}/pages/{page-id}
                endpoint("bb0a08700785be9a509cd5a21a3a267e4ccdb6526998fb3d97ab208f751e28ae", null),
                // GET /v1/tenants/{id}/status/groups
                endpoint("15df46e2ef215be74b173fdfad5d47ff5fcd986354a915070f833b6a8efa3640", null),
                // GET /v1/tenants/{id}/operations-profiles/{profile_id}
                endpoint("857040a5df868e1b714f504f5703b735b328801a85f8982849fc0181c45d8878", null),
                // GET /v1/tenants/{id}/metric-profiles/{profile_id}
                endpoint("bc02a1dcb96f5126eb239de8bf991368986c6039e4a4d7be4ddbf5c07b374ea9", null),
                // POST /v1/tenants/{id}/invitation
                endpoint("6ceed1142e4de1f6e2e9109b8cbbd67e3882044e05f00d2f7c42c4fa8bf65170", null),
                // POST /v1/tenants/{id}/incidents
                endpoint("ef69b61388f0dc183559beb5a31f0c7acc1a5f4e9e2d19e0dd959e735b0c0398", null),
                // PATCH /v1/tenants/{id}/incidents/{incident-id}
                endpoint("a0d8d0b2ed9f7ad0c670585b602b05d867166eb1ae9f46e9761f808782b8d731", Scope.ALL),
                // GET /v1/tenants/{id}/reports/{report-id}/groups
                endpoint("9196d1700fe45d9b0af3ab1659c302363cd23042216166af8f80579973d9c4d4", null),
                // POST /roles/assign
                endpoint("3b9cb06c0103e2544b3138e23c9094e57a76edad2561639b51c47fa8286b82b3", null),
                // GET /v1/tenants/{id}/topology/groups
                endpoint("79d7412d8e2ee1c021d5c211e7cda8c8578cb0fb2fd2d156c23826b4b9c3caa1", null),
                // GET /roles/assigned-endpoints
                endpoint("3409f11008a5c3eb35290ef5ef8da403022212451b791f8eecc3b2be9b6625ba", null),
                // GET /v1/tenants/{id}/report/{report-id}/groups/{group-name}/latest-data
                endpoint("631180bb467ba036f281cec1ea9dfbd72c5df94e508a7dc48665d028ecb72000", null),
                // POST /v1/tenants/{id}/topology/groups
                endpoint("3622181c895fa4dfce6af11e50968c30623102ed45daaa2290e60e923b211d2e", null),
                // GET /v1/tenants/{id}/projects
                endpoint("c216f1aae92607adc816e2db64edc42f041f2e57e01cff1fd50e6d868f253e7f", null),
                // DELETE /v1/tenants/{id}/downtimes/{downtime-id}
                endpoint("ed52b1225bfb9e457e2a0590d97aef6a493957989de2de4bc7b52f73d293d45e", null),
                // GET /v1/tenants/{id}/downtimes
                endpoint("348e947817e81ddcc7e0746c1876be752164fbed01e6e5ff84a289bb7327a511", null),
                // GET /v1/tenants/{id}/reports/{report-id}
                endpoint("bc3655c72754d44890b60fa43656c7f01599c177c3c7ce93f1c1147e37fe9935", null),
                // POST /v1/tenants/{id}/incidents/{incident-id}/comments
                endpoint("735a3bbefd253f4ee961ed2b3e13f9f8af590dff8bd0cb95847bc3468a8a6d0e", null),
                // DELETE /v1/tenants/{id}/pages/{page-id}
                endpoint("1740a59dc9ed2ba930cb77986d2ee313b2533714b141446dee16ffec9655a499", null),
                // GET /v1/tenants/{id}/operations-profiles
                endpoint("4fd6820149f95f3f2f97e02e027a146c9704604748cd034d4272ddf8d8f70a79", null),
                // GET /v1/tenants/{id}/results/{report-name}/groups/{group-name}/endpoints
                endpoint("eeb360f3db4a9796655f276061dde8922c88ca0e733e243d1bbff77f23684017", null),
                // GET /v1/tenants/{id}/aggregation-profiles
                endpoint("6b7273e5e224a2587f2616768c7df71b422003377c7ea33bd98d90d9930832c9", null),
                // GET /v1/tenants/{id}/capabilities/status
                endpoint("de0d5b3369edaae3f0139a0ad3db78a3481b7b6f1f8edee1fd0e79f27143cea6", null),
                // GET /v1/tenants/{id}/status/{report-name}/endpoints
                endpoint("39a3625653c0ee5d9df1ad19349db26d373e110c321a4bc141bcdb2d969f0793", null),
                // DELETE /v1/tenants/{id}/topology/groups
                endpoint("2f1b5ab358719ee79edba67678dc6814bedc1c22837dbdba62bc1ab07802a9eb", null),
                // GET /v1/tenants/{id}/results/{report-name}/endpoints/{endpoint-name}
                endpoint("93cf221cd811b91b69a2c75e2c9f2068b278e3c518903f5e35416e9198c21eab", null),
                // PUT /v1/tenants/{id}/set-node
                endpoint("65f9247833f593ef7d7340c090e1a72906411f63de0eeecd0b9bff88cc3d1a33", null),
                // GET /v1/tenants/{id}/status/{report-name}/groups/{group-name}/endpoints/{endpoint-name}/metrics
                endpoint("c1d4f6c772a0ebabbb0a0aa5215d7631ba157469c4f537e7d780b4b148e0e9e6", null),
                // GET /v1/tenants/{id}/downtimes/{downtime-id}
                endpoint("341e0eb6434a4653099a1dede75f89436d9f95d348b6396ad2ac1f24b0a9cccf", null),
                // GET /v1/tenants/{id}/status/{report-name}/groups/{group-name}/service-types/{service-type-name}/endpoints
                endpoint("03f1c4f872ce1ddcb32bdf0e1289e0735711906a6a7bbdacbebee1932062d35c", null),
                // GET /roles
                endpoint("ad468635b957e2566ca189db34d2351c8a9d9dc710d2ebbadbdbb2af770bd079", null),
                // GET /v1/tenants/{id}/results/{report-name}/supergroups/{supergroup-name}
                endpoint("56d1cd1dcfdcca9f56e7d893be1e843f5c602971f9da29174a5b6bc7861c1118", null),
                // PUT /v1/tenants/{id}/feeds/topology
                endpoint("155c8f805b82a74c1776b581eb33e724d050188f7f897299aeebfabb3ad07320", null),
                // GET /v1/tenants/{id}/status/{report-name}/groups/{group-name}/service-types/{service-type-name}/endpoints/{endpoint-name}/metrics/{metric-name}/details
                endpoint("7a8ef2dd4cb991f0bc088bf17f009e1e0a0876a1ebcc059ec71a3432aa7e08b7", null),
                // GET /v1/tenants/{id}
                endpoint("921fb6a55236e237eb7c08fe7b4c645be3611b36a179d7b6d371c267bf834758", null),
                // GET /v1/tenants/{id}/report/{report-id}/groups/latest-data
                endpoint("2d97ecc21def8f5cfbf0d0db5cd8a598c3523cf16885dfe3259b407beda2208e", null),
                // PATCH /v1/tenants/{id}/incidents/{incident-id}/activity/{activity-id}
                endpoint("aacaa72d7eb7f1bfbfa86d8910ae32f21e183cdad7d25471c974afcc74d39b99", Scope.ALL),
                // GET /v1/tenants/{id}/results/{report-name}/supergroups
                endpoint("0164b575ed48d959debef2282c2f87735432875b800ba3f6767a6146cdf76b87", null)
        );
    }

    private static List<SecuredEndpointAssignment> tenant_viewerEndpoints() {
        return List.of(
                // GET /v1/tenants/{id}/check-readiness
                endpoint("f68ccd41d244c567fb6d308ba3c511007e9679d9b23b6dce6387417b23c29331", null),
                // GET /v1/tenants/{id}/reports
                endpoint("317bf647ef2e8ebc1ddc18e918e8f50e317218b8fbb0c44de13c5b6bf0f7e8cb", null),
                // GET /v1/tenants/{id}/status/{report-name}/groups
                endpoint("94dfc267a2381a7160d2bc3cd1e5f51a74366ceb3449ca430a73eb0ad289d2d0", null),
                // GET /v1/tenants/{id}/aggregation-profiles/{profile_id}
                endpoint("7cff1e350473a2cac2c2a1a2b8d320dce285fe1e9eea4baf24326e86d26d6ba6", null),
                // GET /v1/tenants/{id}/metric-profiles
                endpoint("4bcf5992275fe9c7a23293ca186f0f115c9a71d23dd9dac7c3183a98ba9c01d8", null),
                // GET /v1/tenants/{id}/status
                endpoint("b5d1546f5c5fff86413164d51054cca1f713c3d60a79e114b6ad5569a8bc1ba0", null),
                // GET /v1/tenants/{id}/invitations
                endpoint("39f8988b78ef403c4a75b0449241ea35bfa1f29cf9cb63dcc9b0e782ea32c10c", null),
                // GET /v1/tenants/{id}/results/{report-name}/groups/{group-name}
                endpoint("330b6f873d8b06c0234cc84b64694e67a0a58a0a604b7dd3fc9e0deed03a0049", null),
                // GET /v1/tenants/{id}/status/{report-name}/groups/{group-name}/service-types/{service-type-name}
                endpoint("4a16bb1bda9fa528a2911519402d60a4194899e0b727a28a94c3bf900321bfed", null),
                // GET /v1/tenants/{id}/members
                endpoint("7dbe3cdb0a1e31a897634256adf8363f86beb1a06b01d8f0e12a14bb9383b1f7", null),
                // GET /v1/tenants/{id}/capabilities/availability
                endpoint("c627d1f9330b3599c8bbec3e2750539b1568261a8ee09441db67c3459d7b96b5", null),
                // GET /v1/tenants/{id}/status/{report-name}/groups/{group-name}/endpoints/{endpoint-name}
                endpoint("4269b90b20f48029112a0c3e2aa8ea94f2845f27f9428474cdf8f9a83670922a", null),
                // GET /v1/tenants/{id}/incidents/{incident-id}
                endpoint("88d498c6032fb0c79e09dbfbf200d534fb60361b18b518633f802eba0935724f", null),
                // GET /v1/tenants/{id}/topology/endpoints
                endpoint("96218affb24c27ddce65f240a911db4f4c6416def4ad822ae445de300c26982d", null),
                // GET /v1/tenants/contact-types
                endpoint("5952b8e656d0e9e870f9b4411ca7188e5076f902d246a2225ceeeab407e12105", null),
                // GET /v1/tenants/{id}/status/{report-name}/groups/{group-name}
                endpoint("ca291521f9b414c45c7ca3c6e5117178d77c0ac02170ef344fdebebc759251c4", null),
                // GET /v1/tenants/{id}/incidents/{incident-id}/activity
                endpoint("e2bf37bb28788c2a15f5788484288eb1db65c161365a5a8bc21cffa8de4ccf12", null),
                // GET /v1/tenants/{id}/feeds/topology
                endpoint("37c8ab347ce395ffebe49e7d871ae858481b9d6d6a4d82f4a6e415bcf992cd02", null),
                // GET /v1/tenants/{id}/capabilities/summary/{item}
                endpoint("54a76eb1bbb30d947ea98249fd11bfd840ba2b556f3f2c5eaacf5377e75ca81e", null),
                // GET /v1/tenants/{id}/status/{report-name}/groups/{group-name}/endpoints/{endpoint-name}/metrics/{metric-name}
                endpoint("b920b0918309e77897c93cacb9b5ccbd800ff52e987d4287593ac261ca57a501", null),
                // GET /v1/tenants/{id}/topology/service-types
                endpoint("55adfea0bd473c93e8a4f48ef5a1508d0d54793d0f693e05540f31417a49a030", null),
                // GET /v1/tenants/{id}/status/{report-name}/groups/{group-name}/endpoints
                endpoint("05fa910be90025c9623da020df59ac8836272d072e9fb2c7e29a28cdcbbf35fd", null),
                // GET /v1/tenants/{id}/status/{report-name}/endpoints/{endpoint-name}
                endpoint("e0bb3d931d05541a390bd5f230a612d1f63336990cc6bd61e84a96f153f62474", null),
                // GET /v1/tenants/{id}/results/{report-name}/groups
                endpoint("244bc82fc913ea5aef9aaff212377593bee5a5ce090723ba21df387cfadac5da", null),
                // GET /v1/tenants/{id}/pages
                endpoint("e7757f768a4e07ec2a539b49ba3603733129de77fd133613c545ea3e4de5d4c9", Scope.ALL),
                // GET /v1/tenants/{id}/capabilities/monitoring/metrics
                endpoint("35b25c3fcf450d6b4a305aa730d99a93a55873cd1ee2a72e32722c3ce561c6a9", null),
                // GET /v1/tenants
                endpoint("a0adaa95af8ad1221b569ce2f35ae102f5c8ec4050828408b8258d2adfa2158a", null),
                // GET /v1/tenants/{id}/incidents
                endpoint("d2308a024c55a10f012bf6a69b002c92e7d93c6331dc10d7966157a97b1455e6", null),
                // GET /v1/tenants/{id}/status/{report-name}/groups/{group-name}/service-types/{service-type-name}/endpoints/{endpoint-name}/metrics
                endpoint("ec8361414eb216a1e584e0b7d7aac31c525b9bc410ab5d1042c2a3bfd37da768", null),
                // GET /v1/tenants/{id}/capabilities/monitoring/metrics/{service-id}
                endpoint("d977748a1f2904b07421d2c3b7aba2bf82492e6ed6b2a793f9b7b5805a1d5c64", null),
                // GET /v1/tenants/{id}/pages/check-slug/{slug}
                endpoint("d45036221162673528396c757339c422be4f59339434f4a651a66c29bdee391e", null),
                // GET /v1/tenants/{id}/results/{report-name}/endpoints
                endpoint("86f51239a7598e447c106fed305e138d0b5df197565d2d709478413916185a53", null),
                // GET /v1/tenants/{id}/results/{report-name}/groups/{group-name}/endpoints/{endpoint-name}
                endpoint("c0931d2814ff95b99afa4d16d307c22da6832577d00d0402aa98863e34dc032e", null),
                // GET /v1/tenants/{id}/status/{report-name}/groups/{group-name}/service-types
                endpoint("cadf417f02c6f0ac824b4d00c1324e48a9ceaeb39f60344722ed82260192695b", null),
                // GET /v1/tenants/{id}/results/groups
                endpoint("7372467ae57ba2c1d3a10971b807b89e0eed02f524c8bb356fee82c810323afc", null),
                // GET /v1/tenants/{id}/status/{report-name}/groups/{group-name}/service-types/{service-type-name}/endpoints/{endpoint-name}
                endpoint("3c2d447b06c11f35e1bdc16dfe245495bcdec40879ba475de1636600f65e6602", null),
                // UNRESOLVED: endpoint ID not present in local registry; keep disabled pending review
                // endpoint("daab2c8c74640487d03b8d30b7a28d2785219e61a4b9313a8397d9968b2053a3", null)
                // GET /v1/tenants/{id}/status/{report-name}/groups/{group-name}/service-types/{service-type-name}/endpoints/{endpoint-name}/metrics/{metric-name}
                endpoint("fa8246ff978d19a26591cfe4d429c0e34928e6f28098f2262ac130e3f8ab1fd6", null),
                // GET /v1/tenants/{id}/pages/{page-id}
                endpoint("bb0a08700785be9a509cd5a21a3a267e4ccdb6526998fb3d97ab208f751e28ae", null),
                // GET /v1/tenants/{id}/status/groups
                endpoint("15df46e2ef215be74b173fdfad5d47ff5fcd986354a915070f833b6a8efa3640", null),
                // GET /v1/tenants/{id}/operations-profiles/{profile_id}
                endpoint("857040a5df868e1b714f504f5703b735b328801a85f8982849fc0181c45d8878", null),
                // GET /v1/tenants/{id}/metric-profiles/{profile_id}
                endpoint("bc02a1dcb96f5126eb239de8bf991368986c6039e4a4d7be4ddbf5c07b374ea9", null),
                // GET /v1/tenants/{id}/reports/{report-id}/groups
                endpoint("9196d1700fe45d9b0af3ab1659c302363cd23042216166af8f80579973d9c4d4", null),
                // GET /v1/tenants/{id}/topology/groups
                endpoint("79d7412d8e2ee1c021d5c211e7cda8c8578cb0fb2fd2d156c23826b4b9c3caa1", null),
                // GET /v1/tenants/{id}/projects
                endpoint("c216f1aae92607adc816e2db64edc42f041f2e57e01cff1fd50e6d868f253e7f", null),
                // GET /v1/tenants/{id}/reports/{report-id}
                endpoint("bc3655c72754d44890b60fa43656c7f01599c177c3c7ce93f1c1147e37fe9935", null),
                // GET /v1/tenants/{id}/downtimes
                endpoint("348e947817e81ddcc7e0746c1876be752164fbed01e6e5ff84a289bb7327a511", null),
                // GET /v1/tenants/{id}/operations-profiles
                endpoint("4fd6820149f95f3f2f97e02e027a146c9704604748cd034d4272ddf8d8f70a79", null),
                // GET /v1/tenants/{id}/results/{report-name}/groups/{group-name}/endpoints
                endpoint("eeb360f3db4a9796655f276061dde8922c88ca0e733e243d1bbff77f23684017", null),
                // GET /v1/users/profile
                endpoint("b32ecfacefb21f938fdbb135d02aaaf2782a99558868a87e348157a4788b40e9", null),
                // GET /v1/tenants/{id}/aggregation-profiles
                endpoint("6b7273e5e224a2587f2616768c7df71b422003377c7ea33bd98d90d9930832c9", null),
                // GET /v1/tenants/{id}/capabilities/status
                endpoint("de0d5b3369edaae3f0139a0ad3db78a3481b7b6f1f8edee1fd0e79f27143cea6", null),
                // GET /v1/tenants/{id}/status/{report-name}/endpoints
                endpoint("39a3625653c0ee5d9df1ad19349db26d373e110c321a4bc141bcdb2d969f0793", null),
                // GET /v1/tenants/{id}/results/{report-name}/endpoints/{endpoint-name}
                endpoint("93cf221cd811b91b69a2c75e2c9f2068b278e3c518903f5e35416e9198c21eab", null),
                // GET /v1/tenants/{id}/status/{report-name}/groups/{group-name}/endpoints/{endpoint-name}/metrics
                endpoint("c1d4f6c772a0ebabbb0a0aa5215d7631ba157469c4f537e7d780b4b148e0e9e6", null),
                // GET /v1/tenants/{id}/downtimes/{downtime-id}
                endpoint("341e0eb6434a4653099a1dede75f89436d9f95d348b6396ad2ac1f24b0a9cccf", null),
                // GET /v1/tenants/{id}/status/{report-name}/groups/{group-name}/service-types/{service-type-name}/endpoints
                endpoint("03f1c4f872ce1ddcb32bdf0e1289e0735711906a6a7bbdacbebee1932062d35c", null),
                // GET /v1/tenants/{id}/results/{report-name}/supergroups/{supergroup-name}
                endpoint("56d1cd1dcfdcca9f56e7d893be1e843f5c602971f9da29174a5b6bc7861c1118", null),
                // GET /v1/tenants/{id}/status/{report-name}/groups/{group-name}/service-types/{service-type-name}/endpoints/{endpoint-name}/metrics/{metric-name}/details
                endpoint("7a8ef2dd4cb991f0bc088bf17f009e1e0a0876a1ebcc059ec71a3432aa7e08b7", null),
                // GET /v1/tenants/{id}
                endpoint("921fb6a55236e237eb7c08fe7b4c645be3611b36a179d7b6d371c267bf834758", null),
                // GET /v1/tenants/{id}/results/{report-name}/supergroups
                endpoint("0164b575ed48d959debef2282c2f87735432875b800ba3f6767a6146cdf76b87", null)
        );
    }

    private static List<SecuredEndpointAssignment> automationEndpoints() {
        return List.of(
                // PATCH /v1/automation/tenants/{id}/status
                endpoint("8d8540e85881f5752c704445e6b922c22801ac1cc562f48ea429b5188ee62494", null),
                // GET /v1/automation/tenants/{id}/status
                endpoint("eae699d87cf52178926f1056a0105b4b9976baf86ec48a15cff7f7df3aa0d7eb", null)
        );
    }

    private void assignRole(String username, String roleName, String resourceId) {

        var assignment = new AssignRoleRequest();
        assignment.role = roleName;
        assignment.username = username;

        if (resourceId != null) {
            assignment.apiResource = "Tenant";
            assignment.resourceId = resourceId;
        }

        resourceAuthorizationService.assignRoleToUser(assignment);
    }
}
