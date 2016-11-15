<!-- SCJS 016 START -->

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<%
            String detectorId = request.getParameter("detectorId");
            String detectorName = request.getParameter("detectorName");
            if (detectorName == null) {
%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta http-equiv="REFRESH" content="0; url=Detectors">
        <title>Redirecting.</title>
    </head>
</html>
<%          } else {%>
<html>
    <head>
        <title>Cloud Instation - Detector</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@include file="common/common-head.jsp" %>
        <script type="text/javascript" language="javascript" src="js/detector.js"></script>
        <script type="text/javascript">
            $(document).ready(function(){
                init_common();
                init_detector(decodeURIComponent('<%=detectorName%>'), decodeURIComponent('<%=detectorId%>'));
            });
        </script>
    </head>
    <body>
        <div id="content">
            <%@include file="common/common-banner.jsp" %>
            <%@include file="common/common-links.jsp" %>
            <h1><a href="/Configuration">Configuration</a> / <a href="/Detectors">Detectors</a> / Detector - <span id="detector_name"></span></h1>
            <div class="statistic-group information-panel">
                <label for="description" style="font-weight: bold">Detector Description</label><br>
                <pre id="description" name="description"></pre>
            </div>
            <h2>Engineer Notes Information</h2>
                <p>* - These fields cannot be edited on this page</p>
                <table id="notes-information-table">
                    <thead>
                        <tr>
                            <th>Note Id *</th>
                            <th>Message *</th>
                            <th>Author *</th>
                            <th>Added Timestamp  *</th>
                            <th>Delete</th>
                        </tr>
                    </thead>
                    <tbody>
                    </tbody>
                </table>
                <div class="buttons">
                    <button class="new-note-button">Add new note</button>
                </div>
                <div id="dialog-container">
                    <div id="new-note-dialog" title="Add new note">
                        <p class="message"></p>
                        <form class="jeditable">
                            <fieldset>
                                <label for="description">Description<label for="description" generated="true" class="error"></label></label>
                                <textarea id="description" name="description" type="text" ></textarea>
                                <input id="action" name="action" value="insert" type="hidden"/>
                                <input id="detector_id" name="detector_id" value= <%=detectorId%> type="hidden"/>
                            </fieldset>
                        </form>
                    </div>

                <!-- SCJS 016 START 
                    <div id="Upload" title="Upload file">
                        <p class="message"></p>
                        <h2>Please fill in the file-upload form below</h2>
                        <form action="DetectorLogUpload" method ="post" enctype='multipart/form-data' >
                            <input type=file name=upfile><br>
                            <label>Log description </label>
                            <input type=text name=note ><br>
                            <input id="detector_id" name="detector_id" value= <%=detectorId%> type="hidden"/>
                            <input type=submit value=Upload >
                        </form>

                 SCJS 016 END  -->

                   
                    <div id="notes-information-filter-dialog" class="column-filter-dialog" title="Notes Information Filter">
                    </div>
                    <div id="notes-information-hide-column-dialog" class="column-selection-dialog" title="Notes Information Show/Hide Columns">
                    </div>
                </div>
       </div>
    </body>
</html>
<%          }%>

<!-- SCJS 016 END -->