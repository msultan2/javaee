<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <title>Cloud Instation - Outstation Brands</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@include file="common/common-head.jsp" %>
        <script type="text/javascript" language="javascript" src="js/detector-default-config.js"></script>
        <script type="text/javascript">
            $(document).ready(function () {
                init_common();
                init_detector_default_config();
            });
        </script>
    </head>
    <body>  
        <div id="content">
            <%@include file="common/common-banner.jsp" %>
            <%@include file="common/common-links.jsp" %>
            <h1><a href="/Administration">Administration</a> / DetectorDefaultConfiguration</h1>
            <h2>Configuration Detector Default Configuration</h2>
            <p>* - These fields cannot be edited on this page</p>
            <table id="detector-config-table">
                <thead>
                    <tr>
                        <th>Key </th>
                        <th>Value </th>
                        <th>Delete *</th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>   
            <div class="buttons">
                <button class="new-detector-config-button">Add new detector default configuration</button>
            </div>
            <div id="dialog-container">
                <div id="new-detector-config-dialog" title="Add new key">
                    <p class="message"></p>
                    <form class="jeditable">
                        <fieldset>
                            <label for="property">Detector Key<label for="detector_key" generated="true" class="error"></label></label>
                            <input id="property" name="property" type="text" />
                            <label for="value">Detector Value <label for="value" generated="true" class="error"></label></label>
                            <input id="detector_value" name="value" type="text" />
                            <input id="action" name="action" value="insert" type="hidden"/>
                        </fieldset>
                    </form>
                </div>
                <div id="detector-config-filter-dialog" class="column-filter-dialog" title="Detector Default Configuration Filter">
                </div>
                <div id="detector-config-show-hide-column-dialog" class="column-selection-dialog" title="Detector Default Configuration Show/Hide Columns">
                </div>
            </div>
            <%@include file="common/common-page-footer.jsp" %>
        </div>
    </body>
</html>
