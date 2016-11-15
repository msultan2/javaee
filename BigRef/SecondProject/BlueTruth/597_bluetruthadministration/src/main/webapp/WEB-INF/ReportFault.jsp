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
        <script type="text/javascript" language="javascript" src="js/fault-report.js"></script>
        <script type="text/javascript">            
            $(document).ready(function(){
                init_common();
                init_fault_reports();
            });
        </script>
    </head>
    <body>
        <div id="content">
            <%@include file="common/common-banner.jsp" %>
            <%@include file="common/common-links.jsp" %>
            <h1><a href="/Report">Live Report</a> / Fault Reports</h1>
            <h2>Fault Reports</h2>
            <table id="fault-table">
                <thead>
                    <tr>
                        <th>Report ID</th>
                        <th>Detector Name</th>
                        <th>Report Time</th>
                        <th>Select</th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
            <div id="dialog-container">
                <div id="fault-filter-dialog" class="column-filter-dialog" title="Fault Reports Filter">
                </div>
                <div id="fault-show-hide-column-dialog" class="column-selection-dialog" title="Fault Reports Show/Hide Columns">
                </div>
            </div>
            <%@include file="common/common-page-footer.jsp" %>
        </div>
    </body>
</html>
