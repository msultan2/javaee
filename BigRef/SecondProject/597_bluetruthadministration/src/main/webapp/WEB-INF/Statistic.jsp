<!-- SCJS 016 START -->

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<%
    String reportId = request.getParameter("reportId");
%>
<html>
    <head>
        <title>Cloud Instation - Statistics Report</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@include file="common/common-head.jsp" %>
        <script type="text/javascript" language="javascript" src="js/lib/bluetooth-cod.js"></script>
        <script type="text/javascript" language="javascript" src="js/statistic.js"></script>
        <script type="text/javascript">
            $(document).ready(function() {
                init_common();
                init_statistic(decodeURIComponent('<%=reportId%>'));
            });
        </script>
    </head>
    <body>
        <div id="content">
            <%@include file="common/common-banner.jsp" %>
            <%@include file="common/common-links.jsp" %>
            <h1><a href="/Report">Live Report</a> / <a href="/ReportStatistics">Statistics Reports</a> / View Devices - <span id="detector_name"></span></h1>
            <h2>Device List</h2>
            <table id="statistic-table">
                <thead>
                    <tr>
                        <th>Address or hash</th>
                        <th>COD</th>
                        <th>First Seen</th>
                        <th>Last Seen</th>
                        <th>Obfuscating Function ID</th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
            <div id="dialog-container">
                <div id="statistic-filter-dialog" class="column-filter-dialog" title="Statistics Reports Filter">
                </div>
                <div id="statistic-show-hide-column-dialog" class="column-selection-dialog" title="Statistics Reports Show/Hide Columns">
                </div>
            </div>
        </div>
    </body>
</html>

<!-- SCJS 016 END -->