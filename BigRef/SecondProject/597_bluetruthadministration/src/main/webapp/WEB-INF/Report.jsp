<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <title>Cloud Instation - Report</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">        
        <%@include file="common/common-head.jsp" %>
        <link rel="stylesheet" type="text/css" href="css/occupancy.css" />
        <script type="text/javascript" language="javascript" src="js/journey-report.js"></script>
        <script type="text/javascript" language="javascript" src="js/occupancy-report.js"></script>
        <script type="text/javascript">
            $(document).ready(function() {
                $("#report-link").attr("href", "#");
                $("a", "#sub-links").button();
                init_common();
            });
        </script>
    </head>
    <body>
        <div id="content">
            <%@include file="common/common-banner.jsp" %>
            <%@include file="common/common-links.jsp" %>

            <h1>Report</h1>
            <div id="sub-links">
                <ul>
                    <% if (request.isUserInRole("journey_report")) {%>
                    <li><a id="report-journey-link" href="ReportJourney">Journey Times</a> - View live journey time reports.</li>
                        <% }
                            if (request.isUserInRole("occupancy_report")) {%>
                    <li><a id="occupancy-link" href="ReportOccupancy">Traffic Flow</a> - View live traffic flow reports.</li>
                        <% }
                            if (request.isUserInRole("statistics_report")) {%>
                    <li><a id="statistics-link" href="ReportStatistics">Statistics</a> - View live statistics reports.</li>
                        <% } if (request.isUserInRole("fault_report")) {%>
                    <li><a id="fault-link" href="ReportFault">Faults</a> - View live fault reports.</li>
                        <% } if (request.isUserInRole("detector_status")) {%>
                    <li><a id="status-link" href="DetectorStatus">Detector Status</a> - View live detector status.</li>
                        <% }%>
                </ul>
            </div>
            <%@include file="common/common-page-footer.jsp" %>
        </div>
    </body>
</html>
