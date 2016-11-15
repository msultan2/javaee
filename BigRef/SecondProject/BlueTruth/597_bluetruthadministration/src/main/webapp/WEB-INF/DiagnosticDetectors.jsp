<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <title>Cloud Instation - Detector Diagnostic</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@include file="common/common-head.jsp" %>
        <script type="text/javascript" language="javascript" src="js/diagnostic-detectors.js"></script>
        <script type="text/javascript">
            $(document).ready(function(){
                init_common();
                init_diagnostic_detectors();
            });
        </script>
    </head>
    <body>
        <div id="content">
            <%@include file="common/common-banner.jsp" %>
            <%@include file="common/common-links.jsp" %>
            <h1><a href="/Diagnostic">Diagnostic</a> / Detectors</h1>

            <h2>Configured Detectors</h2>            
            <table id="detector-table">
                <thead>
                    <tr>
                        <th>Detector *</th>
                        <th>ID *</th>
                        <th>Location *</th>
                        <th>Latitude *</th>
                        <th>Longitude *</th>
                        <th>MODE *</th>
                        <th>Carriageway *</th>                        
                        <th>Logical Groups *</th>
                        <th>Status *</th>
                        <th>View Data</th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>           
            <div id="dialog-container">
                <div id="detector-filter-dialog" class="column-filter-dialog" title="Detector Filter">
                </div>
                <div id="detector-show-hide-column-dialog" class="column-selection-dialog" title="Detector Show/Hide Columns">
                </div>
            </div>
            <%@include file="common/common-page-footer.jsp" %>
        </div>
    </body>
</html>
