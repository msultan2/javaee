<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<!-- SCJS 018 START -->

<html>
    <head>
        <title>Cloud Instation - Broadcast Messages</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@include file="common/common-head.jsp" %>
        <script type="text/javascript" language="javascript" src="js/broadcast-messages.js"></script>
        <script type="text/javascript">
            $(document).ready(function(){
                init_common();
                init_broadcast_messages();
            });
        </script>
    </head>
    <body>
        <div id="content">
            <%@include file="common/common-banner.jsp" %>
            <%@include file="common/common-links.jsp" %>
            <h1><a href="/Administration">Administration</a> / Broadcast Messages</h1>
            <h2>Broadcast Messages</h2>
            <table id="broadcast-message-table">
                <thead>
                    <tr>
                        <th>Message Id *</th>
                        <th>Title</th>
                        <th>Description</th>
                        <th>Logical Groups *</th>
                        <th>Delete</th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
            <div class="buttons">
                <button class="new-broadcast-message-button">Add new broadcast message</button>
            </div>
            <div id="dialog-container">
                <div id="new-broadcast-message-dialog" title="Add new broadcast message">
                    <p class="message"></p>
                    <form class="jeditable">
                        <fieldset>
                            <label for="broadcast_name">Title<label for="broadcast_name" generated="true" class="error"></label></label>
                            <input id="broadcast_name" name="broadcast_name" type="text" class="required"/>
                            <label for="description">Description<label for="description" generated="true" class="error"></label></label>
                            <textarea id="description" name="description" class="required"></textarea>
                            <label>Visible Logical Group<label for="logical_group_names" generated="true" class="error"></label></label>
                            <div id="logical-group-select" class="checkbox-select"></div>
                            <input id="action" name="action" value="insert" type="hidden"/>
                        </fieldset>
                    </form>
                </div>
                <div id="broadcast-message-filter-dialog" class="column-filter-dialog" title="Route Filter">
                </div>
                <div id="broadcast-message-show-hide-column-dialog" class="column-selection-dialog" title="Route Show/Hide Columns">
                </div>
            </div>
            <%@include file="common/common-page-footer.jsp" %>
        </div>
    </body>
</html>

<!-- SCJS 018 END -->