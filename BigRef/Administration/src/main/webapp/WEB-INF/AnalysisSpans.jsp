<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Cloud Instation - Span Analysis</title>
        <%@include file="common/common-head.jsp" %>
        <script type="text/javascript" language="javascript" src="js/analysis-spans.js"></script>
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
            <h1><a href="/Analysis">Analysis</a> / Span Analysis</h1>
            <table id="span-table">
                <thead>
                    <tr>
                        <th>Span Name *</th>
                        <th>Start *</th>
                        <th>End *</th>
                        <th>Logical Groups *</th>
                        <th>Duration</th>
                        <th>Speed</th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>            
            <div id="dialog-container">
                <div id="span-filter-dialog" class="column-filter-dialog" title="Span Filter">
                </div>
                <div id="span-show-hide-column-dialog" class="column-selection-dialog" title="Span Show/Hide Columns">
                </div>
            </div>
            <%@include file="common/common-page-footer.jsp" %>
        </div>
    </body>
</html>
