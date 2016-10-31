<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <title>Cloud Instation - Analysis</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@include file="common/common-head.jsp" %>
        <script type="text/javascript">
            $(document).ready(function(){
                init_common();
                $("#analysis-link").attr("href","#");
                $("a", "#sub-links").button();
            });
        </script>
    </head>
    <body>
        <div id="content">
            <%@include file="common/common-banner.jsp" %>
            <%@include file="common/common-links.jsp" %>
            <h1>Analysis</h1>
            <div id="sub-links">
                <ul>
                <li><a id="route-link" href="AnalysisRoutes">Route</a> - View data on routes.</li>
                <li><a id="span-link" href="AnalysisSpans">Span</a> - View data on spans.</li>
                <li><a id="detector-link" href="AnalysisDetectors">Detector</a> - View data on detectors.</li>
                </ul>
            </div>
            <%@include file="common/common-page-footer.jsp" %>
        </div>
    </body>
</html>
