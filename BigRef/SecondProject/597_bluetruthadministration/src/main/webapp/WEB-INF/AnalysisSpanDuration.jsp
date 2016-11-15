<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<%
            String span = request.getParameter("span");
            if (span == null) {
%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta http-equiv="REFRESH" content="0; url=AnalysisSpans">
        <title>Redirecting.</title>
    </head>
</html>
<%          } else {%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Cloud Instation - Analysis / Spans / <%=span%> - Duration</title>
        <%@include file="common/common-head.jsp" %>
        <script type="text/javascript" language="javascript" src="js/analysis-span-duration.js"></script>
        <script type="text/javascript">
            $(document).ready(function(){
                init_common();
                init_analysis_span('<%=span%>');
            });
        </script>
    </head>
    <body>
        <div id="content">
            <%@include file="common/common-banner.jsp" %>
            <%@include file="common/common-links.jsp" %>
            <h1><a href="/Analysis">Analysis</a> / <a href="/AnalysisSpans">Spans</a> / <%=span%> - Duration</h1>

            <div id="graph">
                <img id="loader" src="/img/load.gif" width="64" height="64" style="left:50%; top: 50%; position: absolute">
                <img alt="span-graph" src="#"  id="graph-image"/>
            </div>
            <div class="buttons">
                <button id="graph-control-button">Graph Controls</button>
            </div>

            <h2>Span Graph Data</h2>
            <p>* - These fields cannot be edited on this page</p>
            <table id="span-graph-data-table">
                <thead>
                    <tr>
                        <th>Completed Timestamp *</th>
                        <th>Duration *</th>
                        <th>Mean *</th>
                        <th>Median *</th>
                        <th>Mode *</th>
                        <th>Outlier *</th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>

            <div id="dialog-container">                
                <div id="span-show-hide-column-dialog" class="column-selection-dialog" title="Span Show/Hide Columns">
                </div>
                <div id="span-filter-dialog" class="column-selection-dialog" title="Span Show/Hide Columns">
                </div>

                <div id="graph-controls-dialog" class="control-group information-panel" title="Graph Controls">                    
                    <form id="control-form">
                        <div class="control" id="date-controls">
                            <input id="ui-dialog-auto-focus-hack-fix" type="text"/>
                            <div>
                                <label>Start Date:</label><input id="from" name="from" class="datetimepicker datatable-column-filter-date-min datatable-column-filter span-graph-data-table-filter-date-min-0" index="0" value="<%=yesterday%>">
                            </div>
                            <div>
                                <label>End Date:</label><input name="to" class="datetimepicker datatable-column-filter-date-max datatable-column-filter span-graph-data-table-filter-date-max-0" index="0" value="<%=now%>">
                            </div>
                            <div>
                                <label>Max Duration:</label><input name="max_duration" value="00:30:00" class="timepicker">
                            </div>
                        </div>
                        <hr class="hr-clear-float"/>
                        <div class="control checkbox">
                            <div>
                                <label>Detections:</label><input class="checkbox" name="detections" value="true" type="checkbox" checked="checked">
                            </div>
                            <div>
                                <label>Detection Outliers:</label><input class="checkbox" name="outliers" value="true" type="checkbox" checked="checked">
                            </div>
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
                        <input type="hidden" value="<%=span%>" name="span"/>
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