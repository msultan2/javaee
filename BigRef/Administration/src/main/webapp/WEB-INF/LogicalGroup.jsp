<!-- // SCJS 012 -->

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<%
            String logicalGroupId = request.getParameter("group");
            if (logicalGroupId == null) {
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
        <script type="text/javascript" language="javascript" src="js/logical-group-menu.js"></script>
        <script type="text/javascript">
            $(document).ready(function(){
                init_common();
                var group_name = decodeURIComponent("<%=request.getParameter("group")%>");
                init_logical_group(group_name);
            });
        </script>
    </head>
    <body>
        <div id="content">
            <%@include file="common/common-banner.jsp" %>
            <%@include file="common/common-links.jsp" %>
            <h1><a href="/Administration">User Administration</a> / <a href="/LogicalGroups">Logical Groups</a> / <span id="logical_group"></span></h1>
            <div id="sub-links">
                <ul>
                <li><a id="logical-group-users-link" >Users</a></li>
                <li><a id="logical-group-routes-link" >Routes</a></li>
                <li><a id="logical-group-spans-link" >Spans</a></li>
                <li><a id="logical-group-detectors-link" >Detectors</a></li>
                </ul>
            </div>
            <%@include file="common/common-page-footer.jsp" %>
        </div>
    </body>
</html>
<%          }%>