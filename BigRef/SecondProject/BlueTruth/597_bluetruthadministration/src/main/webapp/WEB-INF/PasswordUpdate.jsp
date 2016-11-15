<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">


<html>
    <head>
        <title>Cloud Instation - Change password</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@include file="common/common-head.jsp" %>
        <script type="text/javascript" language="javascript" src="js/password-update.js"></script>
        <script type="text/javascript">
            $(document).ready(function(){
                init_password_update();
                validate_password();
            });
        </script>
    </head>
    <body>
        <div id="content">
            <%@include file="common/common-banner-password-update.jsp" %>
            <br>
            <h1>Change your password</h1>
            <div id="change-password-dialog">
                <p class="message"></p>
                <form class="password" id="register">
                    <fieldset>
                        <label for="password">New Password</label>
                        <input id="password" name="password" type="password" class="required"/>
                        <span id="result"></span>
                        <label for="password_confirm">New Password (Confirm)</label>
                        <input id="password_confirm" name="password_confirm" type="password" class="required"/>
                        <label for="password_current">Current Password</label>
                        <input id="password_current" name="password_current" type="password" class="required"/>
                        <input type="hidden" name="action" value="password_expire"/>
                    </fieldset>
                </form>
            </div>
            <div id="update-password-button">
                <button class="update-password">Change password </button>
            </div>
        </div>
    </body>
</html>
