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
        <script type="text/javascript" language="javascript" src="js/instation-user.js"></script>
        <script type="text/javascript">
            $(document).ready(function(){
                init_common();
                init_instation_user('<%=selectedUsername%>', <%=request.isUserInRole("role_administration")%>);
            });
        </script>
    </head>
    <body>
        <div id="content">
            <%@include file="common/common-banner.jsp" %>
            <%@include file="common/common-links.jsp" %>
            <h1><a href="/Administration">Administration</a> / <a href="/InstationUsers">Instation Users</a> / [<%=selectedUsername%>]</h1>

            <div id="instation-user-details">
                <!-- SCJS 015 START -->
                <div  style="float:right" id="sub-links">
                    <ul>
                        <%if(request.isUserInRole("brand_administration")){%>
                            <li style="list-style-type: none"><button id="change-brand-button">Change brand</button></li>
                        <%}%>
                <!-- SCJS 015 END -->
                        <li style="list-style-type: none"><button id="change-timezone-button">Change time zone</button></li>
                        <%if(request.isUserInRole("instation_administration")){%>
                            <li style="list-style-type: none"><button id="change-password-expiry-days">Change password expiry days</button></li>
                        <% } %>
                    </ul>
                </div>
                <div>
                    <label>Name:</label><span id="current-user-name"/>
                </div>
                <div>
                    <label>Username:</label><span id="current-user-username"/>
                </div>
                <!-- SCJS 015 START -->
                <%if(request.isUserInRole("brand_administration")){%>
                <div>
                    <label>Brand:</label><span id="current-user-brand"/>
                </div>
                <%}%>
                <!-- SCJS 015 END -->
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
                <div id="user-roles-section">
                    <label>Roles:</label>
                    <ul id="user-roles">
                    </ul>
                </div>
                <div>
                    <div>
                        <label>Logical Group:</label>
                        <ul id="user-logical-groups">
                        </ul>
                    </div>
                </div>    
            </div>            
            <div id="instation-user-role-section">
                <h2>Roles</h2>
                <table id="instation-user-role-table">
                    <thead>
                        <tr>
                            <th>Role Name</th>
                            <th>Description</th>
                            <th>Add/Remove</th>
                        </tr>
                    </thead>
                    <tbody>
                    </tbody>
                </table>
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
                <div id="change-timezone-dialog" title="Change time zone">
                    <p class="message"></p>
                    <form class="jeditable">
                        <fieldset>
                            <label for="timezone_name">Time zone</label>
                            <select id="timezone_name" name="timezone_name"/>
                            <input type="hidden" name="action" value="timezone"/>
                            <input type="hidden" name="username" value="<%=selectedUsername%>"/>
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
                            <input type="hidden" name="username" value="<%=selectedUsername%>"/>
                        </fieldset>
                    </form>
                </div>
                <% } %>
                <div id="instation-user-roles-filter-dialog" class="column-filter-dialog" title="Instation Users Filter">
                </div>
                <div id="instation-user-roles-show-hide-column-dialog" class="column-selection-dialog" title="Instation Users Show/Hide Columns">
                </div>
            </div>
            <%@include file="common/common-page-footer.jsp" %>
        </div>
    </body>
</html>
<%          }%>