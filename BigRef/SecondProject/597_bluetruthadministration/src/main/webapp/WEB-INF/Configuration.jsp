<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <title>Cloud Instation - Configuration</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@include file="common/common-head.jsp" %>
        <script type="text/javascript">
            $(document).ready(function(){
                init_common();
                $("#configuration-link").attr("href","#");
                $("a", "#sub-links").button();
            });
        </script>
    </head>
    <body>
        <div id="content">
            <%@include file="common/common-banner.jsp" %>
            <%@include file="common/common-links.jsp" %>                       
            <h1>Configuration</h1>
            <div id="sub-links">
                <ul>
                <% if (request.isUserInRole("route_configuration")) {%>
                <li><a id="route-link" href="Routes">Routes</a> - Add/Edit/Delete routes.</li>
                <% }
                        if (request.isUserInRole("span_configuration")) {%>
                <li><a id="span-link" href="Spans">Spans</a> - Add/Edit/Delete spans.</li>
                <% }
                        if (request.isUserInRole("detector_configuration")) {%>
                <li><a id="detector-link" href="Detectors">Detectors</a> - Add/Edit/Delete detectors.</li>
                <% }%>
                </ul>
            </div>
            <%@include file="common/common-page-footer.jsp" %>
        </div>
    </body>
</html>
