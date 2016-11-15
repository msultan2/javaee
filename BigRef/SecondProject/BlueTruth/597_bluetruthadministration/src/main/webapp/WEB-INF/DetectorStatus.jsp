<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <title>Cloud Instation - Live Fault Report</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">        
        <%@include file="common/common-head.jsp" %>
        <!--<link rel="stylesheet" type="text/css" href="css/statistics.css" />-->
        <script type="text/javascript" language="javascript" src="js/detector-status.js"></script>
        <script src="http://code.highcharts.com/highcharts.js"></script>
        <script src="http://code.highcharts.com/highcharts-more.js"></script>
        <script src="http://code.highcharts.com/modules/exporting.js"></script>
        <script type="text/javascript">
            $(document).ready(function() {
                init_common();
                init_detector_status();
            });
        </script>
    </head>
    <body>
        <div id="content">
            <%@include file="common/common-banner.jsp" %>
            <%@include file="common/common-links.jsp" %>
            <h1><a href="/Report">Live Report</a> / Detector Status</h1>
            <h2>Fault Reports</h2>
            <table id="status-table">
                <thead>
                    <tr>
                        <th>Detector ID</th>
                        <th>Detector Name</th>
                        <th>Firmware Version</th>
                        <th>Serial Number</th>
                        <th>Configuration Hash</th>
                        <th>2G Signal Level Min</th>
                        <th>2G Signal Level Avg</th>
                        <th>2G Signal Level Max</th>
                        <th>3G Signal Level Min</th>
                        <th>3G Signal Level Avg</th>
                        <th>3G Signal Level Max</th>
                        <th>Performance Index</th>
                        <th>Obfuscating Function</th>
                        <th>Uptime (s)</th>
                        <th>Seed ID</th>
                        <th>SSH</th>
                        <th>Last Updated</th>
                        <th>Machine Voltage</th>
                        <th>View Graph</th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
            <div id="dialog-container">
                <div id="status-filter-dialog" class="column-filter-dialog" title="Device Status Filter">
                </div>
                <div id="status-show-hide-column-dialog" class="column-selection-dialog" title="Device Status Show/Hide Columns">
                </div>
            </div>
            <%@include file="common/common-page-footer.jsp" %>
        </div>
    </body>
</html>
