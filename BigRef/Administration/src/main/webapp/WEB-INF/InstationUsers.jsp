<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <title>Cloud Instation - Instation Users</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@include file="common/common-head.jsp" %>
        <script type="text/javascript" language="javascript" src="js/instation-users.js"></script>
        <script type="text/javascript">
            $(document).ready(function(){
                init_common();
                init_instation_user();
            });
        </script>
    </head>
    <body>
        <div id="content">
            <%@include file="common/common-banner.jsp" %>
            <%@include file="common/common-links.jsp" %>
            <h1><a href="/Administration">Administration</a> / Users</h1>                 
            <table id="instation-user-table">
                <thead>
                    <tr>
                        <th>Name</th>
                        <th>Username</th>
                        <th>Email</th>
                        <th>Roles*</th>
                        <th>Logical Groups*</th>
                        <th>View</th>
                        <th>Activate/Deactivate</th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>            
            <div class="buttons">
                <button class="new-user-button">Add new user</button>
            </div>
            <div id="dialog-container">
                <div id="new-user-dialog" title="Add new user">
                    <p class="message"></p>
                    <form class="jeditable">
                        <fieldset>
                            <label for="full_name">Name:<label for="full_name" generated="true" class="error"></label></label>
                            <input id="full_name" name="full_name" type="text" class="required"/>
                            <label for="username">Username:<label for="username" generated="true" class="error"></label></label>
                            <input id="username" name="username" type="text" class="required"/>
                            <label for="email">Email:<label for="email" generated="true" class="error"></label></label>
                            <input id="email" name="email" type="email" class="email required"/>
                            <label>Role<label for="role_names" generated="true" class="error"></label></label>
                            <div id="role-select" class="checkbox-select"></div>
                            <label>Visible Logical Group<label for="logical_group_names" generated="true" class="error"></label></label>
                            <div id="logical-group-select" class="checkbox-select"></div>
                            <input id="action" name="action" value="insert" type="hidden"/>
                        </fieldset>
                    </form>
                </div>
                <div id="instation-users-filter-dialog" class="column-filter-dialog" title="Instation Users Filter">
                </div>
                <div id="instation-users-show-hide-column-dialog" class="column-selection-dialog" title="Instation Users Show/Hide Columns">
                </div>
            </div>
            <%@include file="common/common-page-footer.jsp" %>
        </div>
    </body>
</html>
