<!-- SCJS 016 START -->

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<%
    String detectorId = request.getParameter("detectorId");
    String detectorName = request.getParameter("detectorName");
    if (detectorId == null) {
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
        <title>Cloud Instation - Manage Detector</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@include file="common/common-head.jsp" %>
        <link rel="stylesheet" type="text/css" href="css/detector-manage.css" />
        <script type="text/javascript" src="http://cdnjs.cloudflare.com/ajax/libs/knockout/2.3.0/knockout-min.js"></script>
        <script type="text/javascript" language="javascript" src="js/lib/notify/notify.min.js"></script>
        <script type="text/javascript" language="javascript" src="js/detector-manage.js"></script>
        <script type="text/javascript">
            $(document).ready(function() {
                init_common();
                init_detector_manage(decodeURIComponent('<%=detectorId%>'),decodeURIComponent('<%=detectorName%>'));
            });
        </script>
    </head>
    <body>
        <div id="content">
            <%@include file="common/common-banner.jsp" %>
            <%@include file="common/common-links.jsp" %>
            <h1><a href="/Configuration">Configuration</a> / <a href="/Detectors">Detectors</a> / Manage Detector - <span id="detector_name"></span></h1>

            <div id="manager-form">
                <h2>Seed</h2>
                <div class="group">
                    <input type="button" value="Generate New Seed" id="seed_gen" />
                    <input type="button" value="Reload Seed" id="seed_change" />
                </div>
                <hr style="border-top: dashed gray 1px">
                <h2>Keys</h2>
                <div class="group">
                    <input type="button" value="Reload keys" id="key_change" />
                </div>
                <hr style="border-top: dashed gray 1px">
                <h2>SSH</h2>
                <div class="group">
                    <div class="ssh-close">
                        <label for="sshopen">Port:</label>
                        <input type="text" name="sshopen" />
                        <input type="button" value="Open" id="ssh_open" />
                    </div>
                    <div class="ssh-open">
                        <input type="button" value="Close" id="ssh_close" />
                    </div>
                </div>
                <hr style="border-top: dashed gray 1px">
                <h2>Background Devices</h2>
                <div class="group">
                    <input type="button" value="Flush" id="flush" />
                    <input type="button" value="Latch" id="latch" />
                </div>
                <hr style="border-top: dashed gray 1px">
                <h2>Outstation To Instation Public Key</h2>
                <div class="group">
                    <input type="button" value="Add" id="add_generic_key" />
                    <input type="button" value="Remove" id="remove_generic_key" />
                </div>
                <hr style="border-top: dashed gray 1px">
                <h2>Other</h2>
                <div class="group">
                    <input type="button" value="Reload Configuration" id="reload" />
                    <input type="button" value="Reboot" id="reboot" />
                    <input type="button" value="Get Status Report" id="get_status" />
                </div>
            </div>

            <div id="command-form">
                <h2>Commands to be sent with the next response</h2>
                <ul data-bind="foreach: commands">
                    <li data-bind="text: to_s()"></li>
                </ul>
                <input type="button" value="Clear" id="cmds_clear" />
                <input type="button" value="Enqueue" id="cmds_send" />
            </div>
        </div>
    </body>
</html>
<%          }%>

<!-- SCJS 016 END -->