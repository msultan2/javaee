<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<%
            String selectedUsername = request.getParameter("username");
            if (selectedUsername == null) {
%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta http-equiv="REFRESH" content="0; url=JourneyReport">
        <title>Redirecting.</title>
    </head>
</html>
<%          } else {%>
<html>
    <head>
        <title>Cloud Instation - Instation User - [<%=selectedUsername%>]</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@include file="common/common-head.jsp" %>
        <script type="text/javascript" language="javascript" src="js/current-instation-user.js"></script>
        <script type="text/javascript">
            $(document).ready(function(){
                init_common();
                init_instation_user('<%=selectedUsername%>');
                validate_password();
            });
        </script>
    </head>
    <body>
        <div id="content">
            <%@include file="common/common-banner.jsp" %>
            <%@include file="common/common-links.jsp" %>
            <h1>Current User - [<%=selectedUsername%>]</h1>

            <div id="instation-user-details">
                <div  style="float:right" id="sub-links">
                    <ul>
                        <li style="list-style-type: none"><button id="change-password-button">Change password</button></li>
                        <li style="list-style-type: none"><button id="change-timezone-button">Change time zone</button></li>
                        <%if(request.isUserInRole("instation_administration")){%>
                            <li style="list-style-type: none"><button id="change-password-expiry-days">Change password expiry days</button></li>
                        <% } %>
                    </ul>
                </div>
                <div>
                    <label>Name:</label><span id="current-user-name"></span>
                </div>
                <div>
                    <label>Username:</label><span id="current-user-username"></span>
                </div>
                <div>
                    <label>Time zone:</label><span id="current-user-timezone">UTC</span>
                </div>
                <div>
                    <label>Email:</label><span id="current-user-email" />
                </div>
                <%if(request.isUserInRole("instation_administration")){%>
                <div>
                    <label>Password update duration:</label><span id="current-user-expiry-days" />
                </div>
                <div>
                    <label>Password will expire in (days):</label><span id="current-user-remaining-days" />
                </div>
                <% } %>

                <div>
                    <label>Roles:</label>
                    <ul id="current-user-roles">
                    </ul>
                </div>
                <div>
                    <label>Logical Groups:</label>
                    <ul id="current-user-logical-groups">
                    </ul>
                </div>                
            </div>            
            <div id="dialog-container">
                <!-- SCJS 015 START -->
                <div id="change-brand-dialog" title="Change brand">
                    <p class="message"></p>
                    <form class="jeditable">
                        <fieldset>
                            <label for="brands">Brand</label>
                            <select id="brands" name="brand"/>
                            <input type="hidden" name="action" value="brand"/>
                            <input type="hidden" name="username" value="<%=selectedUsername%>"/>
                        </fieldset>
                    </form>
                </div>
                <!-- SCJS 015 END -->
                <div id="change-password-dialog" title="Change password">
                    <p class="message"></p>
                    <form class="jeditable" id="register">
                        <fieldset>
                            <label for="password">New Password</label>
                            <input id="password" name="password" type="password" class="required"/>
                            <span id="result"></span>
                            <label for="password_confirm">New Password (Confirm)</label>
                            <input id="password_confirm" name="password_confirm" type="password" class="required"/>
                            <label for="password_current">Current Password</label>
                            <input id="password_current" name="password_current" type="password" class="required"/>
                            <input type="hidden" name="action" value="password"/>
                        </fieldset>
                    </form>
                </div>
                <div id="change-timezone-dialog" title="Change time zone">
                    <p class="message"></p>
                    <form class="jeditable">
                        <fieldset>
                            <label for="timezone_name">Time zone</label>
                            <select id="timezone_name" name="timezone_name"/>
                            <input type="hidden" name="action" value="timezone"/>
                        </fieldset>
                    </form>
                </div>
                <%if(request.isUserInRole("instation_administration")){%>
                <div id="change-password-expiry-days-dialog" title="Change password expiry days">
                    <p class="message"></p>
                    <form class="jeditable">
                        <fieldset>
                            <label for="expiry_days">Expiry days</label>
                            <input id="expiry_days" name="expiry_days" class="required"/>
                            <input type="hidden" name="action" value="expiry_days"/>
                        </fieldset>
                    </form>
                </div>
                <% } %>
            </div>
            <%@include file="common/common-page-footer.jsp" %>
        </div>
    </body>
</html>
<%          }%>