
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<%
    String reportId = request.getParameter("reportId");
%>
<html>
    <head>
        <title>Cloud Instation - Fault Report Messages</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@include file="common/common-head.jsp" %>
        <script type="text/javascript" language="javascript" src="js/lib/bluetooth-cod.js"></script>
        <script type="text/javascript" language="javascript" src="js/fault.js"></script>
        <script type="text/javascript">
            $(document).ready(function() {
                init_common();
                init_fault_messages(decodeURIComponent('<%=reportId%>'));
            });
        </script>
    </head>
    <body>
        <div id="content">
            <%@include file="common/common-banner.jsp" %>
            <%@include file="common/common-links.jsp" %>
            <h1><a href="/Report">Live Report</a> / <a href="/ReportFault">Fault Reports</a> / View Faults - <span id="detector_name"></span></h1>
            <h2>Fault List</h2>
            <table id="fault-messages-table">
                <thead>
                    <tr>
                        <th>Fault Code</th>
                        <th>Fault Description</th>
                        <th>Status</th>
                        <th>Time</th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
            <div id="dialog-container">
                <div id="fault-messages-filter-dialog" class="column-filter-dialog" title="Fault Report Message Filter">
                </div>
                <div id="fault-messages-show-hide-column-dialog" class="column-selection-dialog" title="Fault Report Message Show/Hide Columns">
                </div>
            </div>
        </div>
    </body>
</html>

<!-- SCJS 016 END -->