<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <title>Cloud Instation - Detector Diagnostic</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@include file="common/common-head.jsp" %>
        <script type="text/javascript" language="javascript" src="js/diagnostic-detector.js"></script>
        <script type="text/javascript">
            $(document).ready(function(){
                init_common();
                var detector_id = "<%=request.getParameter("id")%>";
                var diagnostic_detail_role = "<%=request.isUserInRole("diagnostic_detail")%>";
                init_diagnostic_detector(detector_id, diagnostic_detail_role);
            });
        </script>
    </head>
    <body>
        <div id="content">
            <%@include file="common/common-banner.jsp" %>
            <%@include file="common/common-links.jsp" %>
            <h1><a href="/Diagnostic">Diagnostic</a> / <a href="/DiagnosticDetectors">Detectors</a> / <span id="detector-name-crumb"></span> - ID:<span id="detector-id-crumb"></span></h1>

            <div id="detector-diagnostic-data">
                <h1></h1>
                <p id="statistic-update-time"></p>
                <div class="statistic-group information-panel">
                    <div class="statistic">
                        <div>
                            <label>Last Detection:</label><input id="last-device-detection" type="text" readonly="readonly">
                        </div>
                        <div>
                            <label>Total Detections:</label><input id="total-device-detections" type="text" readonly="readonly">
                        </div>
                        <div>
                            <label>Total Detections (Last 5 minutes):</label><input id="total-device-detections-last-5-minutes" type="text" readonly="readonly">
                        </div>
                    </div>
                    <div class="statistic">
                        <div>
                            <label>Last Occupancy Report:</label><input id="last-occupancy-report" type="text" readonly="readonly">
                        </div>
                        <div>
                            <label>Total Occupancy Reports:</label><input id="total-occupancy-reports" type="text" readonly="readonly">
                        </div>
                    </div>
                    <div class="statistic">
                        <div>
                            <label>Last Configuration Download:</label><input id="last-configuration-download-request" type="text" readonly="readonly">
                        </div>
                        <div>
                            <label>Configuration Version:</label><input id="last-configuration-download-version" type="text" readonly="readonly">
                        </div>
                    </div>
                    <hr class="hr-clear-float"/>
                </div>
                <div class="buttons">
                    <button>Refresh Statistics</button>
                </div>

                <% if (request.isUserInRole("diagnostic_detail")) {%>

                <h2>Detector Messages</h2>
                <p>* - These fields cannot be edited on this page</p>
                <table id="detector-message-table">
                    <thead>
                        <tr>
                            <th>Code *</th>
                            <th>Category *</th>
                            <th>Description *</th>
                            <th>Description Detail *</th>
                            <th>Count *</th>
                            <th>Recorded Time*</th>
                        </tr>
                    </thead>
                    <tbody>
                    </tbody>
                </table>
                <h2>Detector Logs</h2>
                <p>* - These fields cannot be edited on this page</p>
                <table id="detector-log-table">
                    <thead>
                        <tr>
                            <th>Detector Log ID *</th>
                            <th>Uploaded Timestamp *</th>
                            <th>Download *</th>
                        </tr>
                    </thead>
                    <tbody>
                    </tbody>
                </table>
                <div id="dialog-container">
                    <div id="detector-message-filter-dialog" class="column-filter-dialog" title="Detector Message Filter">
                    </div>
                    <div id="detector-message-show-hide-column-dialog" class="column-selection-dialog" title="Detector Message Show/Hide Columns">
                    </div>
                    <div id="detector-log-filter-dialog" class="column-filter-dialog" title="Detector Log Filter">
                    </div>
                    <div id="detector-log-show-hide-column-dialog" class="column-selection-dialog" title="Detector Log Show/Hide Columns">
                    </div>
                </div>

                <% } %>
            </div>
            <%@include file="common/common-page-footer.jsp" %>
        </div>
    </body>
</html>
