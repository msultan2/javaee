<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <title>Cloud Instation - Detectors</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@include file="common/common-head.jsp" %>
        <script type="text/javascript" language="javascript" src="js/detectors.js"></script>
        <script type="text/javascript">
            $(document).ready(function(){
                init_common();
                init_detector();                
            });
        </script>
    </head>
    <body>
        <div id="content">
            <%@include file="common/common-banner.jsp" %>
            <%@include file="common/common-links.jsp" %>
            <h1><a href="/Configuration">Configuration</a> / Detectors</h1>            
            <table id="detector-table">
                <thead>
                    <tr>
                        <th>Detector</th>
                        <th>ID</th>
                        <th>Location</th>
                        <th>Latitude</th>
                        <th>Longitude</th>
                        <th>MODE</th>
                        <th>Carriageway</th>
                        <th>Active</th>
                        <th>Logical Groups *</th>
                        <th>Configuration</th>
                        <th>Manage</th>
                        <th>View Data</th>
                        <th>Delete</th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
            <div class="buttons">
                <button class="new-detector-button">Add new detector</button>
            </div>
            <h2>Unconfigured Detectors</h2>
            <h2>* - These fields cannot be edited on this page</h2>
            <table id="unconfigured-detector-table">
                <thead>
                    <tr>
                        <th>ID *</th>
                        <th>Last requested configuration *</th>
                        <th>Last reported detection *</th>
                        <th>Last reported traffic flow *</th>
                        <th>Last message report *</th>
                        <th>Last log upload *</th>
                        <th>Add</th>
                        <th>Delete</th>
                    </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
            <div id="dialog-container">
                <div id="new-detector-dialog" title="Add new detector">
                    <p class="message"></p>
                    <form class="jeditable">
                        <fieldset>
                            <label for="name">Detector Name:<label for="name" generated="true" class="error"></label></label>
                            <input id="name" name="name" class="required" type="text"/>
                            <label for="id">Detector ID:<label for="id" generated="true" class="error"></label></label>
                            <input id="id" name="id" class="required" type="text"/>
                            <label for="latitude">Latitude:<label for="latitude" generated="true" class="error"></label></label>
                            <input id="latitude" name="latitude" class="required number" type="text"/>
                            <label for="logitude">Longitude:<label for="longitude" generated="true" class="error"></label></label>
                            <input id="longitude" name="longitude" class="required number" type="text"/>
                            <label for="mode">MODE:</label>
                            <select id="mode" name="mode" >
                                <option value="0">MODE 0 - Idle</option>
                                <option value="1">MODE 1 - Journey Time</option>
                                <option value="2">MODE 2 - Occupancy</option>
                                <option value="3">MODE 3 - Journey Time & Occupancy</option>
                            </select>
                            <label for="id">Carriageway:</label>
                            <select id="carriageway" name="carriageway">
                                <option value="North">North</option>
                                <option value="South">South</option>
                                <option value="East">East</option>
                                <option value="West">West</option>
                            </select>
                            <label for="id">Active:</label>
                            <input type="checkbox" id="active" name="active">
                            <label>Visible Logical Group<label id="logical_group_names" for="logical_group_names" generated="true" class="error"></label></label>
                            <div id="logical-group-select" class="checkbox-select"></div>
                            <input id="action" name="action" value="insert" type="hidden"/> 
                        </fieldset>
                    </form>
                </div>
                <div id="detector-filter-dialog" class="column-filter-dialog" title="Detector Filter">
                </div>
                <div id="detector-show-hide-column-dialog" class="column-selection-dialog" title="Detector Show/Hide Columns">
                </div>
                <div id="unconfigured-detector-filter-dialog" class="column-filter-dialog" title="Unconfigured Detector Filter">
                </div>
                <div id="unconfigured-detector-show-hide-column-dialog" class="column-selection-dialog" title="Unconfigured Detector Show/Hide Columns">
                </div>
            </div>
            <%@include file="common/common-page-footer.jsp" %>
        </div>
    </body>
</html>
