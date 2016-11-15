<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<%
            String route = request.getParameter("route");
            if (route == null) {
%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta http-equiv="REFRESH" content="0; url=AnalysisRoutes">
        <title>Redirecting.</title>
    </head>
</html>
<%          } else {%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Cloud Instation - Analysis / Routes / <%=route%> - Duration</title>
        <%@include file="common/common-head.jsp" %>
        <script type="text/javascript" language="javascript" src="js/analysis-route-duration.js"></script>
        <script type="text/javascript">
            $(document).ready(function(){
                init_common();
                init_analysis_route('<%=route%>');
            });
        </script>
    </head>
    <body>
        <div id="content">
            <%@include file="common/common-banner.jsp" %>
            <%@include file="common/common-links.jsp" %>
            <h1><a href="/Analysis">Analysis</a> / <a href="/AnalysisRoutes">Routes</a> / <%=route%> - Duration</h1>

            <div id="graph">
                <img id="loader" src="/img/load.gif" width="64" height="64" style="left:50%; top: 50%; position: absolute">
                <img alt="route-graph" src="#"  id="graph-image"/>
            </div>
            <div class="buttons">
                <button id="graph-control-button">Graph Controls</button>
            </div>
            <div class="buttons">
                <button id="graph-download-csv">Download Graph CSV</button>
            </div>

            <h2>Route Graph Data</h2>
            <p>* - These fields cannot be edited on this page</p>
            <table id="route-graph-data-table">
                <thead>
                    <tr>
                        <th>Span Name *</th>
                        <th>Completed Timestamp *</th>
                        <th>Mean *</th>
                        <th>Median *</th>
                        <th>Mode *</th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>

            <div id="dialog-container">                
                <div id="route-show-hide-column-dialog" class="column-selection-dialog" title="Route Show/Hide Columns">
                </div>
                <div id="route-filter-dialog" class="column-selection-dialog" title="Route Show/Hide Columns">
                </div>
                
                <div id="graph-controls-dialog" class="control-group information-panel" title="Graph Controls">                    
                    <form id="control-form">
                        <div class="control" id="date-controls">
                            <input id="ui-dialog-auto-focus-hack-fix" type="text"/>
                            <div>
                                <label>Start Date:</label><input id="from" name="from" class="datetimepicker datatable-column-filter-date-min datatable-column-filter route-graph-data-table-filter-date-min-1" index="1" value="<%=yesterday%>">
                            </div>
                            <div>
                                <label>End Date:</label><input name="to" class="datetimepicker datatable-column-filter-date-max datatable-column-filter route-graph-data-table-filter-date-max-1" index="1" value="<%=now%>">
                            </div>
                        </div>
                        <hr class="hr-clear-float"/>
                        <div class="control checkbox">
                            <div>
                                <label>Mean:</label><input class="checkbox" name="average" value="mean" type="radio">
                            </div>
                            <div>
                                <label>Median:</label><input class="checkbox" name="average" value="median" type="radio" checked="checked">
                            </div>
                            <div>
                                <label>Mode:</label><input class="checkbox" name="average" value="mode" type="radio">
                            </div>
                        </div>
                        <div class="control checkbox">
                            <div>
                                <label>Display Total Only</label><input class="checkbox" name="total" value="true" type="checkbox">
                            </div>
                        </div>
                        <input type="hidden" value="<%=route%>" name="route"/>
                        <input type="hidden" value="640" name="width" id="graph_width"/>
                        <input type="hidden" value="480" name="height" id="graph_height"/>
                    </form>                    
                </div> 
            </div>

            <%@include file="common/common-page-footer.jsp" %>
        </div>
    </body>
</html>
<%          }%>