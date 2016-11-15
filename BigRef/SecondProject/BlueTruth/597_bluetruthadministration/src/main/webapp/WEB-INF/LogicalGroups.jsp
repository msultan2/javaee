<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <title>Cloud Instation - Logical Groups</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@include file="common/common-head.jsp" %>
        <script type="text/javascript" language="javascript" src="js/logical-groups.js"></script>
        <script type="text/javascript">
            $(document).ready(function(){
                init_common();
                init_logical_groups();
            });
        </script>
    </head>
    <body>
        <div id="content">
            <%@include file="common/common-banner.jsp" %>
            <%@include file="common/common-links.jsp" %>
            <h1><a href="/Administration">Administration</a> / Logical Groups</h1>
            <h2>Logical Group Definitions</h2>
            <p>* - These fields cannot be edited on this page</p>  
            <table id="logical-group-table">
                <thead>
                    <tr>
                        <th>Logical Group Name</th>
                        <th>Description</th>
                        <th>View</th>
                        <th>Delete</th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
            <div class="buttons">
                <button class="new-logical-group-button">Add new logical group</button>
            </div>
            <div id="dialog-container">
                <div id="new-logical-group-dialog" title="Add new logical group">
                    <p class="message"></p>
                    <form class="jeditable">
                        <fieldset>
                            <label for="logical_group_name">Logical Group Name</label>
                            <input id="logical_group_name" name="logical_group_name" type="text" class="required" />
                            <label for="description">Description</label>
                            <textarea id="description" name="description" class="required"></textarea>
                            <input id="action" name="action" value="insert" type="hidden"/>
                        </fieldset>
                    </form>
                </div>
                <div id="logical-groups-filter-dialog" class="column-filter-dialog" title="Logical Groups Filter">
                </div>
                <div id="logical-groups-show-hide-column-dialog" class="column-selection-dialog" title="Logical Groups Show/Hide Columns">
                </div>
            </div>
            <%@include file="common/common-page-footer.jsp" %>
        </div>
    </body>
</html>
