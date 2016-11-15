<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <title>Cloud Instation - Spans</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@include file="common/common-head.jsp" %>
        <script type="text/javascript" language="javascript" src="js/spans.js"></script>
        <script type="text/javascript">
            $(document).ready(function(){
                init_common();
                init_span();
            });
        </script>
    </head>
    <body>
        <div id="content">
            <%@include file="common/common-banner.jsp" %>
            <%@include file="common/common-links.jsp" %>
            <h1><a href="/Configuration">Configuration</a> / Spans</h1>
            <table id="span-table">
                <thead>
                    <tr>
                        <th>Span Name</th>
                        <th>Start</th>
                        <th>End</th>
                        <th>Total Distance (Metres)</th>
                        <th>Stationary (MPH)</th>
                        <th>Very Slow (MPH)</th>
                        <th>Slow (MPH)</th>
                        <th>Moderate (MPH)</th>
                        <th>Journey Time Limit (Mins)</th>
                        <th>Logical Groups *</th>
                        <th>View Data</th>
                        <th>Select</th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
            <div class="buttons">
                <button class="new-span-button">Add new span</button>
            </div>
            <div id="dialog-container">
                <div id="new-span-dialog" title="Add new span">
                    <p class="message"></p>
                    <form class="jeditable">
                        <fieldset>
                            <label for="span_name">Span Name<label for="span_name" generated="true" class="error"></label></label>
                            <input id="span_name" name="span_name" type="text" />
                            <label for="start_detector_id">Start Detector<label for="start_detector_id" generated="true" class="error"></label></label>
                            <select id="start_detector_id" name="start_detector_id"></select>
                            <label for="end_detector_id">End Detector<label for="end_detector_id" generated="true" class="error"></label></label>
                            <select id="end_detector_id" name="end_detector_id"></select>
                            <label for="stationary">Stationary (in MPH)<label for="stationary" generated="true" class="error"></label></label>
                            <input id="stationary" name="stationary" type="text" value="5"/>
                            <label for="very_slow">Very Slow (in MPH)<label for="very_slow" generated="true" class="error"></label></label>
                            <input id="very_slow" name="very_slow" type="text" value="10"/>
                            <label for="slow">Slow (in MPH)<label for="slow" generated="true" class="error"></label></label>
                            <input id="slow" name="slow" type="text" value="20"/>
                            <label for="moderate">Moderate (in MPH)<label for="moderate" generated="true" class="error"></label></label>
                            <input id="moderate" name="moderate" type="text" value="30"/>
                            <%  if (request.isUserInRole("journey_configuration")) { %>
                                <label for="journey_time_limit">Journey Time Limit (in Mins)<label for="journey_time_limit" generated="true" class="error"></label></label>
                                <input id="journey_time_limit" name="journey_time_limit" type="text" value="60" />
                            <%  } else { %>
                                <label for="journey_time_limit">Journey Time Limit (in Mins)<label for="journey_time_limit" generated="true" class="error"></label></label>
                                <input id="journey_time_limit" name="journey_time_limit" type="text" value="60" disabled/>
                            <%  } %>
                            <label>Visible Logical Group<label for="logical_group_names" generated="true" class="error"></label></label>
                            <div id="logical-group-select" class="checkbox-select"></div>
                            <input id="action" name="action" value="insert" type="hidden"/>
                        </fieldset>
                    </form>
                </div>
                <div id="span-filter-dialog" class="column-filter-dialog" title="Span Filter">
                </div>
                <div id="span-show-hide-column-dialog" class="column-selection-dialog" title="Span Show/Hide Columns">
                </div>
            </div>
            <%@include file="common/common-page-footer.jsp" %>
        </div>
    </body>
</html>
