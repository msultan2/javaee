
<!-- SCJS 008 -->


<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<%
            String spanName = request.getParameter("spanName");
            if (spanName == null) {
%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta http-equiv="REFRESH" content="0; url=Spans">
        <title>Redirecting.</title>
    </head>
</html>
<%          } else {%>
<html>
    <head>
        <title>Cloud Instation - Span</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@include file="common/common-head.jsp" %>
        <script type="text/javascript" language="javascript" src="js/span.js"></script>
        <script type="text/javascript">
            $(document).ready(function(){
                init_common();
                init_span(decodeURIComponent('<%=spanName%>'));
            });
        </script>
    </head>
    <body>
        <div id="content">
            <%@include file="common/common-banner.jsp" %>
            <%@include file="common/common-links.jsp" %>
            <h1><a href="/Configuration">Configuration</a> / <a href="/Spans">Spans</a> / Span - <span id="span_name"></span></h1>

            <div id="span-information-data">
                <h2>Notes Information</h2>
                <p>* - These fields cannot be edited on this page</p>
                <table id="notes-information-table">
                    <thead>
                        <tr>
                            <th>Note Id *</th>
                            <th>Description *</th>
                            <th>Author *</th>
                            <th>Added Timestamp *</th>
                            <th>Delete</th>
                        </tr>
                    </thead>
                    <tbody>
                    </tbody>
                </table>
                <div class="buttons">
                    <button class="new-note-button">Add new note</button>
                </div>
                <h2>Events Information</h2>
                <p>* - These fields cannot be edited on this page</p>
                <table id="events-information-table">
                    <thead>
                        <tr>
                            <th>Event Id *</th>
                            <th>Description *</th>
                            <th>Start Timestamp  *</th>
                            <th>End Timestamp *</th>
                            <th>Delete</th>
                        </tr>
                    </thead>
                    <tbody>
                    </tbody>
                </table> 
                <div class="buttons">
                    <button class="new-event-button">Add new event</button>
                </div>

                <h2>Incidents Information</h2>
                <p>* - These fields cannot be edited on this page</p>
                <table id="incidents-information-table">
                    <thead>
                        <tr>
                            <th>Incident Id *</th>
                            <th>Description *</th>
                            <th>Start Timestamp  *</th>
                            <th>End Timestamp *</th>
                            <th>Delete</th>
                        </tr>
                    </thead>
                    <tbody>
                    </tbody>
                </table>
                <div class="buttons">
                    <button class="new-incident-button">Add new incident</button>
                </div>

                <div id="dialog-container">
                    <div id="new-note-dialog" title="Add new note">
                        <p class="message"></p>
                        <form class="jeditable">
                            <fieldset>
                                <label for="description">Description<label for="description" generated="true" class="error"></label></label>
                                <textarea id="description" name="description" type="text" ></textarea>
                                <input id="action" name="action" value="insert" type="hidden"/>
                                <input id="span_name" name="span_name" value= <%=spanName%> type="hidden"/>
                            </fieldset>
                        </form>
                    </div>
                    <div id="new-event-dialog" title="Add new event">
                        <p class="message"></p>
                        <form class="jeditable">
                            <fieldset>
                                <label for="description">Description<label for="description" generated="true" class="error"></label></label>
                                <textarea id="description" name="description" type="text" ></textarea>
                                <label for="start_timestamp">Start Timestamp<label for="start_timestamp" generated="true" class="error"></label></label>
                                <input id="start_timestamp" name="startTimestamp" class="datetimepicker" type="datetime">
                                <label for="end_timestamp">End Timestamp<label for="end_timestamp" generated="true" class="error"></label></label>
                                <input id="end_timestamp" name="endTimestamp" class="datetimepicker" type="datetime">
                                <input id="action" name="action" value="insert" type="hidden"/>
                                <input id="span_name" name="span_name" value= <%=spanName%> type="hidden"/>
                            </fieldset>
                        </form>
                    </div>
                    <div id="new-incident-dialog" title="Add new incident">
                        <p class="message"></p>
                        <form class="jeditable">
                            <fieldset>
                                <label for="description">Description<label for="description" generated="true" class="error"></label></label>
                                <textarea id="description" name="description" type="text" ></textarea>
                                <label for="start_timestamp1">Start Timestamp<label for="start_timestamp1" generated="true" class="error"></label></label>
                                <input id="start_timestamp1" name="startTimestamp1" class="datetimepicker" type="datetime">
                                <label for="end_timestamp1">End Timestamp<label for="end_timestamp1" generated="true" class="error"></label></label>
                                <input id="end_timestamp1" name="endTimestamp1" class="datetimepicker" type="datetime">
                                <input id="action" name="action" value="insert" type="hidden"/>
                                <input id="span_name" name="span_name" value= <%=spanName%> type="hidden"/>
                            </fieldset>
                        </form>
                    </div>
                    <div id="notes-information-filter-dialog" class="column-filter-dialog" title="Notes Information Filter">
                    </div>
                    <div id="notes-information-hide-column-dialog" class="column-selection-dialog" title="Notes Information Show/Hide Columns">
                    </div>
                    <div id="incidents-information-filter-dialog" class="column-filter-dialog" title="Incidents Information Filter">
                    </div>
                    <div id="incidents-information-hide-column-dialog" class="column-selection-dialog" title="Incidents Information Show/Hide Columns">
                    </div>
                    <div id="events-information-filter-dialog" class="column-filter-dialog" title="Events Information Filter">
                    </div>
                    <div id="events-information-hide-column-dialog" class="column-selection-dialog" title="Events Information Show/Hide Columns">
                    </div>

                </div>
            </div>
            <%@include file="common/common-page-footer.jsp" %>
        </div>
    </body>
</html>
<%          }%>