<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <title>Cloud Instation - Live Statistics Report</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">        
        <%@include file="common/common-head.jsp" %>
        <link rel="stylesheet" type="text/css" href="css/statistics.css" />
        <script type="text/javascript" language="javascript" src="js/statistics-report.js"></script>
        <script type="text/javascript">            
            $(document).ready(function(){
                init_common();
                init_statistics_reports();
            });
        </script>
    </head>
    <body>
        <div id="content">
            <%@include file="common/common-banner.jsp" %>
            <%@include file="common/common-links.jsp" %>
            <h1><a href="/Report">Live Report</a> / Statistics Reports</h1>
            <h2>Statistics Reports</h2>
            <table id="statistics-table">
                <thead>
                    <tr>
                        <th>Report ID</th>
                        <th>Detector Name</th>
                        <th>Report Start</th>
                        <th>Report End</th>
                        <th>Select</th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
            <div id="dialog-container">
                <div id="statistics-filter-dialog" class="column-filter-dialog" title="Statistics Reports Filter">
                </div>
                <div id="statistics-show-hide-column-dialog" class="column-selection-dialog" title="Statistics Reports Show/Hide Columns">
                </div>
            </div>
            <%@include file="common/common-page-footer.jsp" %>
        </div>
    </body>
</html>
