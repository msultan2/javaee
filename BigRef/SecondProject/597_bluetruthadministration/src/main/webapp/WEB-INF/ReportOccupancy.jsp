<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <title>Cloud Instation - Live Traffic Flow Report</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">        
        <%@include file="common/common-head.jsp" %>
        <link rel="stylesheet" type="text/css" href="css/occupancy.css" />
        <script type="text/javascript" language="javascript" src="js/occupancy-report.js"></script>
        <script type="text/javascript">            
            $(document).ready(function(){
                init_common();
                init_occupancy_reports();
            });
        </script>
    </head>
    <body>
        <div id="content">
            <%@include file="common/common-banner.jsp" %>
            <%@include file="common/common-links.jsp" %>
            <h1><a href="/Report">Live Report</a> / Traffic Flow</h1>
            <h2>Traffic Flow</h2>
            <table id="occupancy-table">
                <thead>
                    <tr>
                        <th>Detector ID</th>
                        <th>Detector Name</th>
                        <th>Reported Timestamp</th>
                        <th>Stationary</th>
                        <th>Very Slow</th>
                        <th>Slow</th>
                        <th>Moderate</th>
                        <th>Free</th>
                        <th>Queue Status</th>
                        <th>Logical Groups</th>
                        <th>Select</th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
            <div id="graphs">
                <h1></h1>
                <ol>
                    <li><a id="a-occupancy-last-hour" href="#h2-occupancy-last-hour"></a></li>
                    <li><a id="a-occupancy-last-day" href="#h2-occupancy-last-day"></a></li>
                </ol>               
                
                <h1 id="h2-occupancy-last-hour" name="h2-occupancy-last-hour"></h1>
                <p id="p-occupancy-last-hour-loading">LOADING...</p>
                <img id="occupancy-last-hour" alt="graph" src="#"/>
                <a class="top-links" href="#content">Back to top of page</a>
                <h1 id="h2-occupancy-last-day" name="h2-occupancy-last-day"></h1>
                <p id="p-occupancy-last-day-loading">LOADING...</p>
                <img id="occupancy-last-day" alt="graph" src="#"/>
                <a class="top-links" href="#content">Back to top of page</a>
            </div>
            <div id="dialog-container">
                <div id="occupancy-filter-dialog" class="column-filter-dialog" title="Traffic Flow Filter">
                </div>
                <div id="occupancy-show-hide-column-dialog" class="column-selection-dialog" title="Traffic Flow Show/Hide Columns">
                </div>
            </div>
            <%@include file="common/common-page-footer.jsp" %>
        </div>
    </body>
</html>
