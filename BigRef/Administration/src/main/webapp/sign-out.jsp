<%@page import="ssl.bluetruth.utils.AuditTrailProcessor"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
            if (request.getUserPrincipal() != null) {
                String username = request.getUserPrincipal().getName();
                AuditTrailProcessor.log(username, AuditTrailProcessor.UserAction.USER_LOGOUT, "User logged out");
            }
            session.invalidate();
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta http-equiv="REFRESH" content="0; url=/Home">
        <link rel="stylesheet" type="text/css" href="css/eric-meyer-reset.min.css" />
        <link rel="stylesheet" type="text/css" href="css/base.css" />
        <link rel="stylesheet" type="text/css" href="css/jquery.dataTables.css" />
        <link rel="stylesheet" type="text/css" href="css/jquery.dataTables_themeroller.css" />
        <link rel="stylesheet" type="text/css" href="css/jeditable.dataTables.css" />
        <link rel="stylesheet" type="text/css" href="css/default-theme/jquery-ui-1.8.22.custom.css" />
        <title>Signing out of application.</title>
    </head>
    <body>
        <div id="content">
            <h1>Signing out of application. If the login page is not displayed in the next 5 seconds click <a href="/Home">here</a></h1>
        </div>
    </body>
</html>
