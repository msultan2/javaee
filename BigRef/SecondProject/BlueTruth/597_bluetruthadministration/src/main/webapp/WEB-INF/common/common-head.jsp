<%@page import="ssl.bluetruth.chart.common.*"%>
<link rel="icon" type="image.png" href="http://www.bluetruth.co.uk/img/bluetooth.png" />
<link rel="stylesheet" type="text/css" href="css/eric-meyer-reset.min.css" />
<link rel="stylesheet" type="text/css" href="css/base.css" />
<link rel="stylesheet" type="text/css" href="css/jquery.dataTables.css" />
<link rel="stylesheet" type="text/css" href="css/jquery.dataTables_themeroller.css" />
<link rel="stylesheet" type="text/css" href="css/jeditable.dataTables.css" />
<link rel="stylesheet" type="text/css" href="css/default-theme/jquery-ui-1.8.22.custom.css" />
<% if(session.getAttribute("css_url") != null) {%>
<link rel="stylesheet" type="text/css" href="<%=session.getAttribute("css_url")%>" />
<%}%>
<link rel="stylesheet" type="text/css" href="css/custom.dataTables.css" />
<link rel="stylesheet" type="text/css" href="css/jquery-ui-timepicker-addon.css" />

<script type="text/javascript" language="javascript" src="js/jquery-1.7.2.min.js"></script>
<script type="text/javascript" language="javascript" src="js/jquery-ui-1.8.20.custom.min.js"></script>
<script type="text/javascript" language="javascript" src="js/jquery.dataTables.min.js"></script>
<script type="text/javascript" language="javascript" src="js/jquery.jeditable.mini.js"></script>
<script type="text/javascript" language="javascript" src="js/jquery.validate.min.js"></script>
<script type="text/javascript" language="javascript" src="js/jquery-ui-timepicker-addon.js"></script>
<script type="text/javascript" language="javascript" src="js/common.js"></script>
<script type="text/javascript" language="javascript" src="js/lib/underscore.min.js"></script>
<%
            TimeSource timeSource = new TimeSourceImpl();
            DateTimeHelper dth = new DateTimeHelper(timeSource);

            String now = dth.getCurrentLocalTimestamp();
            String yesterday = dth.getYesterdayTimestamp();
%>