<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <title>Cloud Instation - Routes</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@include file="common/common-head.jsp" %>
        <link rel="stylesheet" href="css/routes.css" type="text/css">
        <script type="text/javascript" language="javascript" src="js/routes.js"></script>
        <script type="text/javascript">
            $(document).ready(function(){
                init_common();
                init_routes();
            });
        </script>
    </head>
    <body>
        <div id="content">
            <%@include file="common/common-banner.jsp" %>
            <%@include file="common/common-links.jsp" %>
            <h1><a href="/Configuration">Configuration</a> / Routes</h1>
            <table id="routes-table">
                <thead>
                    <tr>
                        <th>Route Name</th>
                        <th>Description</th>
                        <th>Logical Groups *</th>
                        <th>View</th>
                        <th>Delete</th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
            <div class="buttons">
                <button class="new-route-button">Add new route</button>
            </div>
            <div id="dialog-container">
                <div id="new-route-dialog" title="Add new route">
                    <p class="message"></p>
                    <form class="jeditable">
                        <fieldset>
                            <label for="route_name">Route Name<label for="route_name" generated="true" class="error"></label></label>
                            <input id="route_name" name="route_name" type="text" class="required"/>
                            <label for="description">Description<label for="description" generated="true" class="error"></label></label>
                            <textarea id="description" name="description" class="required"></textarea>
                            <label>Visible Logical Group<label id="logical_group_names" for="logical_group_names" generated="true" class="error"></label></label>
                            <div id="logical-group-select" class="checkbox-select"></div>
                            <input id="action" name="action" value="insert" type="hidden"/>
                        </fieldset>
                    </form>
                </div>
                <div id="route-filter-dialog" class="column-filter-dialog" title="Route Filter">
                </div>
                <div id="route-show-hide-column-dialog" class="column-selection-dialog" title="Route Show/Hide Columns">
                </div>
            </div>
            <%@include file="common/common-page-footer.jsp" %>
        </div>
    </body>
</html>
