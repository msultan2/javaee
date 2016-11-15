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
        <script type="text/javascript" language="javascript" src="js/logical-group-spans.js"></script>
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
            <h1><a href="/Administration">User Administration</a> / <a href="/LogicalGroups">Logical Groups</a> / <span id="logical_group_name"><a href="#"></a></span> / Spans </h1>
            <h2>Logical Group Spans</h2>
            <table id="span-logical-group-table">
                <thead>
                    <tr>
                        <th>Span Name</th>
                        <th>Start</th>
                        <th>End</th>
                        <th>Add/Remove</th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
            <div id="dialog-container">
                <div id="span-filter-dialog" class="column-filter-dialog" title="Span Filter">
                </div>
                <div id="span-show-hide-column-dialog" class="column-selection-dialog" title="Span Show/Hide Columns">
                </div>
             </div>
            </div>
    </body>
</html>
<%          }%>