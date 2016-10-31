<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <title>Cloud Instation - Administration</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@include file="common/common-head.jsp" %>
        <script type="text/javascript">
            $(document).ready(function () {
                init_common();
                $("a", "#sub-links").button();
            });
        </script>
    </head>
    <body>
        <div id="content">
            <%@include file="common/common-banner.jsp" %>
            <%@include file="common/common-links.jsp" %>
            <h1>Administration</h1>
            <div id="sub-links">
                <ul>
                    <%
                        if (request.isUserInRole("user_administration")) {%>
                    <li><a id="instation-user-link" href="InstationUsers">Users</a> - Add/Edit/Delete instation users.</li>
                        <% }
                            if (request.isUserInRole("role_administration")) {%>
                    <li><a id="roles-link" href="InstationRoles">User Roles</a> - View user roles available.</li>
                        <% }
                            if (request.isUserInRole("logical_group_administration")) {%>
                    <li><a id="logical-groups-link" href="LogicalGroups">Logical Groups</a> - Add/Edit/Delete logical groups.</li>
                        <% }%>
                        <%
                            //SCJS 015 START
                            if (request.isUserInRole("brand_administration")) {%>
                    <li><a id="instation-brand-link" href="Brands">Brands</a> - Add/Edit/Delete brands.</li>
                        <% }
                            // SCJS 015 END
                            // SCJS 018 START
                            if (request.isUserInRole("broadcast_message_administration")) {%>
                    <li><a id="broadcast-message-link" href="BroadcastMessages">Broadcast Messages</a> - Add/Edit/Delete broadcast messages.</li>
                        <% }
                            // SCJS 018 END
                            if (request.isUserInRole("support_administration")) {%>
                    <li><a id="support-admin-link" href="AdministrationSupport">Support</a> - Add/Edit/Delete email/phone contacts.</li>
                        <% }
                            if (request.isUserInRole("audit_trail_administration")) {%>
                    <li><a id="audit-trail-link" href="AuditTrail">Audit Trail</a> - View user audit trail.</li>
                        <% }
                            if (request.isUserInRole("user_administration")) {%>
                    <li><a id="audit-trail-link" href="DetectorDefaultConfiguration">Default Detector Configuration</a> - Add/Edit/Delete default detector config.</li>
                        <% }%>
                </ul>
            </div>
            <%@include file="common/common-page-footer.jsp" %>
        </div>
    </body>
</html>
