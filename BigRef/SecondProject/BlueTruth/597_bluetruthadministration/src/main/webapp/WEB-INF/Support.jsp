<%-- 
    Document   : support
    Created on : 05-Dec-2012, 14:25:54
    Author     : wingc
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <title>Cloud Instation - Support</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@include file="common/common-head.jsp" %>
        <script type="text/javascript" language="javascript" src="js/support.js"></script>
        <script type="text/javascript">
            $(document).ready(function(){
                init_common();
                init_support_page();
            });
        </script>
    </head>
    <body>
        <div id="content">
            <%@include file="common/common-banner.jsp" %>
            <%@include file="common/common-links.jsp" %>
            <h1>Cloud Instation Support</h1>
            <p>You can contact support using any of the methods detailed in this page.</p>
            <div id="support-content">
                <div id="contact-email-section"></div>
                <div id="contact-telephone-section"></div>
            </div>
            <%@include file="common/common-page-footer.jsp" %>
        </div>
    </body>
</html>
