<!-- // SCJS 012 -->

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<%
            String logicalGroupName = request.getParameter("group");
            if (logicalGroupName == null) {
%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta http-equiv="REFRESH" content="0; url=LogicalGroups">
        <title>Redirecting.</title>
    </head>
</html>
<%          } else {%>
<html>
    <head>
        <title>Cloud Instation - Logical Group</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@include file="common/common-head.jsp" %>
        <script type="text/javascript" language="javascript" src="js/logical-group-users.js"></script>
        <script type="text/javascript">
            $(document).ready(function(){
                init_common();
                var logical_group_name = decodeURIComponent('<%=logicalGroupName%>');
                init_logical_group(logical_group_name);                
            });
        </script>
    </head>
    <body>
        <div id="content">
            <%@include file="common/common-banner.jsp" %>
            <%@include file="common/common-links.jsp" %>
            <h1><a href="/Administration">User Administration</a> / <a href="/LogicalGroups">Logical Groups</a> / <span id="logical_group_name"><a href="#"></a></span> / Users </h1>

            <h2>Logical Group Users</h2>
            <table id="instation-user-logical-group-table">
                <thead>
                    <tr>
                        <th>Name</th>
                        <th>Username</th>
                        <th>Add/Remove</th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
            </div>
            <div id="dialog-container">
                <div id="instation-users-filter-dialog" class="column-filter-dialog" title="Instation Users Filter">
                </div>
                <div id="instation-users-show-hide-column-dialog" class="column-selection-dialog" title="Instation Users Show/Hide Columns">
                </div>
            </div>
   </body>
</html>
<%          }%>