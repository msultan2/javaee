<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<%
    String detector_id = request.getParameter("detector_id");
    if (detector_id == null) {
%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta http-equiv="REFRESH" content="0; url=AnalysisDetectors">
        <title>Redirecting.</title>
    </head>
</html>
<%          } else {%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Cloud Instation - Analysis / Detectors /  <%=detector_id%> - Detections</title>
        <%@include file="common/common-head.jsp" %>
        <script type="text/javascript" language="javascript" src="js/analysis-detector-detections.js"></script>
        <script type="text/javascript">
            $(document).ready(function(){
                init_common();
                init_analysis_detector('<%=detector_id%>');
            });
        </script>
    </head>
    <body>
        <div id="content">
            <%@include file="common/common-banner.jsp" %>
            <%@include file="common/common-links.jsp" %>
            <h1><a href="/Analysis">Analysis</a> / <a href="/AnalysisDetectors">Detectors</a> / <%=detector_id%> - Detections</h1>

            <div id="graph">
                <img id="loader" src="/img/load.gif" width="64" height="64" style="left:50%; top: 50%; position: absolute">
                <img alt="detector-graph" src="#"  id="graph-image"/>
            </div>
            <div class="buttons">
                <button id="graph-control-button">Graph Controls</button>
            </div>

            <h2>Detector Graph Data</h2>
            <p>* - These fields cannot be edited on this page</p>
            <table id="detector-graph-data-table">
                <thead>
                    <tr>
                        <th>Time Period *</th>
                        <th>Count *</th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>

            <div id="dialog-container">                
                <div id="detector-show-hide-column-dialog" class="column-selection-dialog" title="Detector Show/Hide Columns">
                </div>
                <div id="detector-filter-dialog" class="column-selection-dialog" title="Detector Show/Hide Columns">
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
                        </div>
                        <hr class="hr-clear-float"/>
                        <div class="control checkbox">
                            <div>
                                <label>Unique Detections:</label><input class="checkbox" name="unique" value="true" type="checkbox">
                            </div>                            
                        </div>
                        <input type="hidden" value="<%=detector_id%>" name="detector_id"/>
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