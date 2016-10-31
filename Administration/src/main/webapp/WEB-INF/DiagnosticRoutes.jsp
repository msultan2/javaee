
<!-- SCJS 014 START -->

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <title>Cloud Instation - Diagnostic Routes</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@include file="common/common-head.jsp" %>
        <script type="text/javascript" language="javascript" src="js/diagnostic-routes.js"></script>
        <script type="text/javascript">
            $(document).ready(function(){
                init_common();
                init_routes();
            });
        </script>
    </head>
    <body>
        <div id="content">
            <%@include file="common/common-banner.jsp" %>
            <%@include file="common/common-links.jsp" %>
            <h1><a href="/Diagnostic">Diagnostic</a> / Diagnostic Routes</h1>
            <h2>Route Definitions</h2>
            <table id="routes-table">
                <thead>
                    <tr>
                        <th>Route Name</th>
                        <th>Description</th>
                        <th>Logical Groups *</th>
                        <th>Status *</th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
            <div id="dialog-container">
                <div id="diagnostic-route-filter-dialog" class="column-filter-dialog" title="Route Filter">
                </div>
                <div id="diagnostic-route-show-hide-column-dialog" class="column-selection-dialog" title="Route Show/Hide Columns">
                </div>
            </div>
            <%@include file="common/common-page-footer.jsp" %>
        </div>
    </body>
</html>

<!-- SCJS 014 END -->