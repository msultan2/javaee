<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <title>Cloud Instation - Detector Configuration</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@include file="common/common-head.jsp" %>
        <script type="text/javascript" language="javascript" src="js/detector-configuration.js"></script>
        <script type="text/javascript">
            $(document).ready(function(){
                init_common();
                var detector_id = decodeURIComponent("<%=request.getParameter("id")%>");
                init_detector_configuration(detector_id);
            });


        </script>
    </head>
    <body>
        <div id="content">
            <%@include file="common/common-banner.jsp" %>
            <%@include file="common/common-links.jsp" %>
            <h1><a href="/Configuration">Configuration</a> / <a href="Detectors">Detectors</a> / <span id="detector-name-crumb"></span> Configuration - ID:<span id="detector-id-crumb"></span></h1>
            
            <h1>Configuration</h1>
            <div class="detector-configuration-edit-buttons">
                <button class="save-button">Save Configuration</button>
            </div>
            <form id="configuration-form">
            </form>
            <div class="detector-configuration-edit-buttons">
                <button class="save-button">Save Configuration</button>
            </div>
            <%@include file="common/common-page-footer.jsp" %>
        </div>
    </body>
</html>
