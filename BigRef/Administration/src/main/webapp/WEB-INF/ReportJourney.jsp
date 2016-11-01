<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <title>Cloud Instation - Live Journey Time Report</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">        
        <%@include file="common/common-head.jsp" %>
        <script type="text/javascript" language="javascript" src="js/journey-report.js"></script>
        <script type="text/javascript">            
            $(document).ready(function(){
                init_common();
                init_journey_reports(); 
            });
        </script>
    </head>
    <body>
        <div id="content">
            <%@include file="common/common-banner.jsp" %>
            <%@include file="common/common-links.jsp" %>
            <h1><a href="/Report">Live Report</a> / Journey Times</h1>
            <h2>Average Journey Times</h2>
            <table id="journey-time-table">
                <thead>
                    <tr>
                        <th>Route *</th>
                        <th>Journey Time *</th>
                        <th>Average Speed *</th>
                        <th>Strength *</th>
                        <th>Status *</th>
                        <th>Calculated Timestamp *</th>
                        <th>Logical Groups *</th>
                    </tr>
                </thead>
                <tbody>
                <tbody>
            </table>
            <div id="dialog-container">
                <div id="journey-time-filter-dialog" class="column-filter-dialog" title="Journey Time Filter">
                </div>
                <div id="journey-time-show-hide-column-dialog" class="column-selection-dialog" title="Journey Time Show/Hide Columns">
                </div>
            </div>
            <%@include file="common/common-page-footer.jsp" %>
        </div>
    </body>
</html>
