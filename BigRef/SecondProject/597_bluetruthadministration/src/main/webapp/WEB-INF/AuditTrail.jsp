<%-- 
    Document   : AuditTrail
    Created on : 12-Dec-2012, 12:08:44
    Author     : wingc
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <title>Audit Trail</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@include file="common/common-head.jsp" %>
        <script type="text/javascript" language="javascript" src="js/audit-trail.js"></script>
        <script type="text/javascript">
            $(document).ready(function(){
                init_common();
                init_audit_trail();
            });
        </script>
    </head>
    <body>
        <div id="content">
            <%@include file="common/common-banner.jsp" %>
            <%@include file="common/common-links.jsp" %>
            <h1><a href="/Administration">Administration</a> / Audit Trail</h1>
            <table id="audit-trail-table">
                <thead>
                    <tr>
                        <th>Audit Trail Id *</th>
                        <th>Username</th>
                        <th>Timestamp</th>
                        <th>Action</th>
                        <th>Description</th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
            <div id="dialog-container">
                <div id="audit-trail-filter-dialog" class="column-filter-dialog" title="Audit Trail Filter">
                </div>
                <div id="audit-trail-show-hide-column-dialog" class="column-selection-dialog" title="Audit Trail Show/Hide Columns">
                </div>
            </div>
            <%@include file="common/common-page-footer.jsp" %>
        </div>
    </body>
</html>
