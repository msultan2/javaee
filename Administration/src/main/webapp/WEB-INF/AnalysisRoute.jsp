<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Cloud Instation - Route Analysis</title>
        <%@include file="common/common-head.jsp" %>
        <script type="text/javascript" language="javascript" src="js/analysis-route.js"></script>
        <script type="text/javascript">
            $(document).ready(function(){
                init_common();
                init_analysis();
            });
        </script>
    </head>
    <body>
        <div id="content">
            <%@include file="common/common-banner.jsp" %>
            <%@include file="common/common-links.jsp" %>
            <h1><a href="/Analysis">Analysis</a> / Route Analysis</h1>
            <h2>Routes</h2>
            <table id="routes-table">
                <thead>
                    <tr>
                        <th>Route Name *</th>
                        <th>Description *</th>
                        <th>Logical Groups *</th>
                        <th>Select</th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
            <div id="graphs">
                <h1></h1>
                <ol>
                    <li><a id="a-last-hour" href="#h2-last-hour"></a></li>
                    <li><a id="a-last-day" href="#h2-last-day"></a></li>
                </ol>
                <h1 id="h2-last-hour" name="h2-last-hour"></h1>
                <p id="p-last-hour-loading">LOADING...</p>
                <img id="last-hour" alt="graph" src="#"/>
                <a class="top-links" href="#content">Back to top of page</a>
                <h1 id="h2-last-day" name="h2-last-day"></h1>
                <p id="p-last-day-loading">LOADING...</p>
                <img id="last-day" alt="graph" src="#"/>
                <a class="top-links" href="#content">Back to top of page</a>
            </div>
            <div id="dialog-container">
                <div id="route-filter-dialog" class="column-filter-dialog" title="Route Filter">
                </div>
                <div id="route-show-hide-column-dialog" class="column-selection-dialog" title="Route Show/Hide Columns">
                </div>
            </div>
            <%@include file="common/common-page-footer.jsp" %>
        </div>
    </body>
</html>
