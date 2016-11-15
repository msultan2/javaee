<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <title>Cloud Instation - Home</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@include file="common/common-head.jsp" %>
        <script type="text/javascript" language="javascript" src="js/home.js"></script>
        <script type="text/javascript">
            $(document).ready(function(){
                init_common();
                $("a", "#sub-links").button();
                init_home();
            });
        </script>
    </head>
    <body>
        <div id="content">
            <%@include file="common/common-banner.jsp" %>
            <%@include file="common/common-links.jsp" %>
            <div id="broadcast-messages"></div>
            <h1>Bluetooth Detector Cloud Instation</h1>
            <p style="width:800px">The Bluetooth Detector Cloud Instation System is a non intrusive above ground traffic flow detector system. This system allows information on journey times, traffic speed, flow characteristics and road occupancy can be calculated and displayed using data derived from Bluetooth signals emanating from passing traffic.</p>
            <h1>Instation Sections</h1>
            <div id="sub-links">
                <ul>  
                    <% if (request.isUserInRole("journey_report") || request.isUserInRole("occupancy_report")) {%>
                    <li>
                        <a id="report-link" href="Report">Report</a> - View real time reports on journey time and traffic flow.
                    </li>
                    <% }
                                if (request.isUserInRole("map_interface")) {%>
                    <li>
                        <a id="map-link" href="Map">Map</a> - View geographic layout of detectors and journey calculations.
                    </li>
                    <% }
                                if (request.isUserInRole("analysis")) {%>
                    <li>
                        <a id="analysis-link" href="Analysis">Analysis</a>  - Analyse recorded data.
                    </li>
                    <% }
                                if (request.isUserInRole("diagnostic")) {%>
                    <li>
                        <a id="diagnostic-link" href="Diagnostic">Diagnostic</a> - Diagnose problems on Detectors, Spans and Routes.
                    </li>
                    <% }
                                if (request.isUserInRole("route_configuration")
                                        || request.isUserInRole("span_configuration")
                                        || request.isUserInRole("detector_configuration")) {%>
                    <li>
                        <a id="configuration-link" href="Configuration">Configuration</a> - Configure Detectors, Spans and Routes.
                    </li>                    
                    <% }
                                if (request.isUserInRole("user_administration")
                                        || request.isUserInRole("role_administration")
                                        || request.isUserInRole("logical_group_administration")) {%>
                    <li>
                        <a id="administration-link" href="Administration">Administration</a> - Administrate user information.
                    </li>
                     <% }
                                if (request.isUserInRole("wiki_view")
                                    || request.isUserInRole("wiki_administration")) {%>
                    <li>
                        <a id="wiki-link" href="Wiki">Wiki</a> - Wiki page.
                    </li>
                    <% }%>
                    <li>
                        <a id="support-link" href="Support">Support</a> - Contact support concerning any issues with system.
                    </li>
                </ul>
            </div>
            <%@include file="common/common-page-footer.jsp" %>
        </div>
    </body>
</html>
