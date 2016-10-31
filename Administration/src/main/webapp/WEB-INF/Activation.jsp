
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Cloud Instation - User activation</title>
        <%@include file="common/common-head.jsp" %>
        <%@include file="common/common-banner-password-update.jsp" %>
        <%
            String action = request.getParameter("action");
            String activation_key = request.getParameter("act_key");
            if (action == null || activation_key == null) {
        %>
            <br>
            <h1 class ="error"> Invalid URL </h1>

        <% } else {%>
            <script type="text/javascript" language="javascript" src="js/activate-user.js"></script>
            <script type="text/javascript">
            $(document).ready(function(){
                    init_activate_user('<%=action%>', '<%=activation_key%>');
            });
        <% } %>
        </script>
    </head>
    <body>
        <br>
        <h1><span id ="activation-status"/></h1>
    </body>
</html>
