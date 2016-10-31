<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<%
            String routeName = request.getParameter("route");
            if (routeName == null) {
%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta http-equiv="REFRESH" content="0; url=JourneyReport">
        <title>Redirecting.</title>
    </head>
</html>
<%          } else {%>
<html>
    <head>
        <title>Cloud Instation - Route - <%=routeName%></title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@include file="common/common-head.jsp" %>
        <script type="text/javascript" language="javascript" src="js/route.js"></script>
        <script type="text/javascript">
            $(document).ready(function(){
                init_common();
                var route_name = decodeURIComponent('<%=routeName%>');
                init_route_spans(route_name);
            });
        </script>
    </head>
    <body>
        <div id="content">
            <%@include file="common/common-banner.jsp" %>
            <%@include file="common/common-links.jsp" %>
            <h1><a href="/Configuration">Configuration</a> / <a href="Routes">Routes</a> / Route - <span id ="routeName"></span></h1>

            <h2>Route Spans</h2>
            <table id="route-spans-table">
                <thead>
                    <tr>
                        <th>Span Name *</th>
                        <th>Logical Groups</th>
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
            <%@include file="common/common-page-footer.jsp" %>
        </div>
    </body>
</html>
<%          }%>