package org.grnet.status.services;

import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.grnet.status.enums.InvitationStatus;
import org.grnet.status.enums.MailType;
import org.jboss.logging.Logger;

import java.util.HashMap;
import java.util.List;

/**
 * Service responsible for sending system notification emails using predefined templates.
 */
@ApplicationScoped
public class MailerService {

    private static final Logger LOG = Logger.getLogger(MailerService.class);

    @Inject
    Mailer mailer;

    @ConfigProperty(name = "api.server.url")
    String serviceUrl;

    @ConfigProperty(name = "quarkus.mailer.from")
    String sendFrom;

    @ConfigProperty(name = "quarkus.mailer.reply-to")
    String replyTo;

    @ConfigProperty(name = "api.mail.title", defaultValue = "ARGO MON Status")
    String title;

    @Inject
    @Location("tenant_invitation.html")
    Template tenantInvitationTemplate;

    @Inject
    @Location("tenant_invitation_response_notify_user.html")
    Template tenantInvitationAcceptTemplate;

    @Inject
    @Location("tenant_invitation_response_notify_admin.html")
    Template tenantInvitationNotifyAdminTemplate;

    @Inject
    @Location("tenant_added_to_group.html")
    Template tenantAddedToGroupTemplate;

    @Inject
    @Location("incident_created.html")
    Template incidentCreatedTemplate;

    /**
     * Sends a tenant invitation email to the specified recipients.
     *
     * @param recipientEmail list of recipient email addresses
     * @param tenantName tenant name
     * @param role role assigned in the invitation
     * @param invitationUrl invitation acceptance URL
     */
    public void sendTenantInvitationEmail(List<String> recipientEmail,
                                          String tenantName,
                                          String role,
                                          String invitationUrl) {

        HashMap<String, Object> params = new HashMap<>();
        params.put("sendFrom", sendFrom);
        params.put("replyTo", replyTo);
        params.put("title", title);

        var resolvedLogo = serviceUrl + "/v1/images/logo.png";
        params.put("logoUrl", resolvedLogo);

        params.put("tenantName", tenantName);
        params.put("role", role);
        params.put("invitationUrl", invitationUrl);

        sendBcc(tenantInvitationTemplate, params, MailType.TENANT_INVITATION_CREATED, recipientEmail);
    }


    /**
     * Sends a notification email to the invitee after accepting a tenant invitation.
     *
     * @param recipientEmail list of recipient email addresses
     * @param tenantName tenant name
     * @param role assigned role
     * @param uiBaseUrl application base URL
     */
    public void sendInvitationAcceptedToInvitee(List<String> recipientEmail,
                                                String tenantName,
                                                String role, String uiBaseUrl) {

        HashMap<String, Object> params = new HashMap<>();
        var resolvedLogo = serviceUrl + "/v1/images/logo.png";
        params.put("logoUrl", resolvedLogo);

        params.put("sendFrom", sendFrom);
        params.put("replyTo", replyTo);
        params.put("title", title);
        params.put("tenantName", tenantName);
        params.put("role", role);
        params.put("uiUrl", uiBaseUrl);


        sendBcc(tenantInvitationAcceptTemplate, params, MailType.TENANT_INVITATION_RESPONSE_NOTIFY_USER, recipientEmail);
    }

    /**
     * Sends an email notification to a user after being added to a tenant group.
     *
     * @param recipientEmail list of recipient email addresses
     * @param tenantName tenant name
     * @param role assigned role
     * @param uiBaseUrl application base URL
     */
    public void sendEmailToMemberAddedGroup(List<String> recipientEmail,
                                                String tenantName,
                                                String role, String uiBaseUrl) {

        HashMap<String, Object> params = new HashMap<>();
        var resolvedLogo = serviceUrl + "/v1/images/logo.png";
        params.put("logoUrl", resolvedLogo);

        params.put("replyTo", replyTo);
        params.put("title", title);
        params.put("tenantName", tenantName);
        params.put("role", role);
        params.put("uiUrl", uiBaseUrl);


        sendBcc(tenantAddedToGroupTemplate, params, MailType.TENANT_ACCESS_GRANTED_USER, recipientEmail);
    }

    /**
     * Sends a notification email to tenant administrators about an invitation response.
     *
     * @param adminEmails list of administrator email addresses
     * @param tenantName tenant name
     * @param inviteeEmail invitee email address
     * @param role invitation role
     * @param status invitation status
     * @param uiBaseUrl application base URL
     */
    public void sendInvitationResponseToAdmins(List<String> adminEmails,
                                               String tenantName,
                                               String inviteeEmail,
                                               String role,
                                               InvitationStatus status,
                                               String uiBaseUrl) {

        HashMap<String, Object> params = new HashMap<>();

        var resolvedLogo = serviceUrl + "/v1/images/logo.png";
        params.put("logoUrl", resolvedLogo);

        params.put("sendFrom", sendFrom);
        params.put("replyTo", replyTo);
        params.put("title", title);
        params.put("tenantName", tenantName);
        params.put("inviteeEmail", inviteeEmail);
        params.put("role", role);
        params.put("status", status.name());
        params.put("uiUrl", uiBaseUrl);

        sendBcc(tenantInvitationNotifyAdminTemplate, params, MailType.TENANT_INVITATION_RESPONSE_NOTIFY_ADMIN, adminEmails);
    }

    /**
     * Sends an email notification when a new incident is reported.
     *
     * @param recipientEmails list of recipient email addresses
     * @param incidentNumber human-readable incident number
     * @param incidentTitle incident title
     * @param description incident description
     * @param serviceNames affected service names
     * @param status incident status
     * @param createdBy user who reported the incident
     * @param createdAt incident creation timestamp
     * @param incidentUrl incident details URL
     */
    public void sendIncidentCreatedEmail(List<String> recipientEmails,
                                          String incidentNumber,
                                          String incidentTitle,
                                          String description,
                                          List<String> serviceNames,
                                          String status,
                                          String createdBy,
                                          String createdAt,
                                          String incidentUrl) {

        HashMap<String, Object> params = new HashMap<>();

        var resolvedLogo = serviceUrl + "/v1/images/logo.png";

        params.put("logoUrl", resolvedLogo);
        params.put("replyTo", replyTo);
        params.put("title", title);

        params.put("incidentNumber", incidentNumber);
        params.put("incidentTitle", incidentTitle);
        params.put("description", description);
        params.put("serviceNames", serviceNames);
        params.put("status", status);
        params.put("createdBy", createdBy);
        params.put("createdAt", createdAt);
        params.put("incidentUrl", incidentUrl);

        sendBcc(incidentCreatedTemplate, params, MailType.INCIDENT_CREATED, recipientEmails);
    }

    /**
     * Sends an email using the provided template and parameters to the specified recipients.
     *
     * @param template email template
     * @param params template parameters
     * @param mailType mail type configuration
     * @param recipients list of recipient email addresses
     */
    private void sendBcc(Template template,
                         HashMap<String, Object> params,
                         MailType mailType,
                         List<String> recipients) {

        var mailTemplate = mailType.execute(template, params);

        var mail = new Mail();
        mail.setHtml(mailTemplate.getBody());
        mail.setSubject(mailTemplate.getSubject());
        mail.setBcc(recipients);
        mail.setReplyTo(replyTo);

        try {
            mailer.send(mail);
            LOG.infof("Email sent to %s (subject=%s)", recipients, mail.getSubject());
        } catch (Exception e) {
            LOG.errorf(e, "Failed to send email to %s (subject=%s)", recipients, mail.getSubject());       }
    }
}
