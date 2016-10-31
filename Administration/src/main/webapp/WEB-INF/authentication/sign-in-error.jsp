<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta http-equiv="Cache-Control" content="no-store,no-cache,must-revalidate">
        <meta http-equiv="Pragma" content="no-cache">
        <meta http-equiv="Expires" content="-1">
        <%@include file="../common/common-head.jsp" %>
        <title>Cloud Instation - Sign In</title>
        <script type="text/javascript" language="javascript" src="js/forgot-password.js"></script>
        <script type="text/javascript">
            $(document).ready(function(){
                $("#sign-in-button").button();
                init_forgot_password();
            });
        </script>
    </head>
    <body>
        <div id="content">
            <%@include file="../common/common-banner.jsp" %>
            <form id="sign-in-form" action="j_security_check" method="POST">
                <label>Username:</label><input type="text" name="j_username"/>
                <label>Password:</label><input type="password" name="j_password"/>
                <input id="sign-in-button" type="submit" value="Sign in" />  
                <br />
                <p style="display:inline;" class="error">Error: the username or password is incorrect.</span>
            </form>
            <a id="forgotPassword" href="#">forgot password?</a>
            <div id="dialog-container">
                <div id="forgot-password-dialog" title="Change password">
                    <p class="message"></p>
                    <form class="jeditable">
                        <fieldset>
                            <label for="username">Enter username<label for="username" generated="true" class="error"></label></label>
                            <input id="username" name="username" type="username" class="required"/>
                            <label for="email">Enter email address <label for="email" generated="true" class="error"></label></label>
                            <input id="email" name="email" type="email" class="required"/>
                            <input type="hidden" name="action" value="forgotPassword"/>
                        </fieldset>
                    </form>
                </div>
            </div>
        </div>        
    </body>
</html>
