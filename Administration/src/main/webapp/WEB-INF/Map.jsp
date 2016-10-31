<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <title>Cloud Instation - Map</title>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <%@include file="common/common-head.jsp" %>
        <link rel="stylesheet" href="leaflet/leaflet.css" type="text/css">
        <link rel="stylesheet" type="text/css" href="leaflet/MarkerCluster.css" />
        <link rel="stylesheet" type="text/css" href="leaflet/MarkerCluster.Default.css" />
        <!--[if lte IE 8]>
        <link rel="stylesheet" href="leaflet/leaflet.ie.css" type="text/css">
        <![endIf]-->
        <link rel="stylesheet" href="css/map.css" type="text/css">
        <script type="text/javascript" src="leaflet/leaflet.js"></script>
        <script type="text/javascript" src="leaflet/leaflet.markercluster.js"></script>
        
        <script type="text/javascript" src="js/map.js"></script>

        <script type="text/javascript">
            $(document).ready(function(){
                init_common();

                var analysis = <%=request.isUserInRole("analysis")%>;
                var diagnostic = <%=request.isUserInRole("diagnostic")%>;
                var detector_configuration = <%=request.isUserInRole("detector_configuration")%>;
                var span_configuration = <%=request.isUserInRole("span_configuration")%>;

                $("#layers-options").hide();
                $("#tools-options").hide();

                $("#map-link").attr("href","#");
                init_map({
                    permissions:{
                        analysis:analysis,
                        diagnostic:diagnostic,
                        detector_configuration:detector_configuration,
                        span_configuration:span_configuration
                    }
                });
            });
        </script>
    </head>
    <body>
        <div id="content">
            <%@include file="common/common-banner.jsp" %>
            <%@include file="common/common-links.jsp" %>
            <div id="add-detector-controls">
                <h1>Add new detector</h1>
                <p>Select the location on the map where the new detector is to be added.</p>
                <div id="add-detector-control-buttons"><button id="add-detector-confirm">Confirm Detector Location</button><button id="add-detector-cancel">Cancel Adding Detector</button></div>
            </div>
            <div id="edit-detector-location-controls">
                <h1>Edit detector location</h1>
                <p>Move the detector to the correct location on the map.</p>
                <div id="edit-detector-location-control-buttons"><button id="edit-detector-location-confirm">Confirm Detector Location</button><button id="edit-detector-location-cancel">Cancel Editing Detector Location</button></div>
            </div>
            <div id="edit-span-route-controls">
                <h1>Edit span route</h1>
                <p>Move the start and end markers to the correct location on the map. If you need to add way points for the route, click the location on the map. To remove a way point click on it a second time.</p>
                <div id="edit-span-route-control-buttons"><button id="edit-span-route-confirm">Confirm Span Route</button><button id="edit-span-route-cancel">Cancel Editing Span Route</button></div>
            </div>
            <div id="map-controls">
                <input id="filters-button" type="checkbox"/><label for="filters-button">Filters</label>
                <input id="layers-button" type="checkbox"/><label for="layers-button">Layers</label>
                <div id="layers-options" class="ui-widget-content">                    
                    <div>
                        <label>Detectors:</label><input id="detector-layer" type="checkbox" checked="checked"/>
                    </div>
                    <div>
                        <label>Spans:</label><input id="span-layer" type="checkbox" checked="checked"/>
                    </div>
                    <div>
                        <label>Routes:</label><input id="route-layer" type="checkbox" checked="checked"/>
                    </div>
                    <div>
                        <label>Clustering:</label><input id="clustering-layer" type="checkbox" checked="checked"/>
                    </div>
                    <div class="heading">
                        Span Color
                    </div>
                    <div id="span-color-options">
                        <div>
                            <label>Speed:</label><input name="span-color" value="speed" type="radio" checked="checked"/>
                        </div>
                        <div>
                            <label>Status:</label><input name="span-color" value="status" type="radio"/>
                        </div>
                    </div>
                    <div style="clear:both"></div>
                </div>
                <input id="tools-button" type="checkbox"/><label id="tools-button-label" for="tools-button">Tools</label>
                <div id="tools-options">
                    <button id="add-new-detector-button">Add new detector</button>
                </div>
                <div id="legendDiv">
                    <legend id="legend">
                        <h1>Legend</h1>
                        <table id="legendTable">
                            <tr>
                                <td><img  src="leaflet/images/detector-green.png" height="25" width="25"></td>
                                <td><span id="legend-green" >green</span></td>
                            </tr>
                            <tr>
                                <td><img src="leaflet/images/detector-blue.png" height="25" width="25"></td>
                                <td><span id="legend-blue">blue</span></td>
                            </tr>
                            <tr>
                                <td><img src="leaflet/images/detector-grey.png" height="25" width="25"></td>
                                <td><span id="legend-grey">grey</span></td>
                            </tr>
                        </table>
                    </legend>
                </div>
            </div>
            <div id="map-canvas">
            </div>
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
                            <label>Visible Logical Group<label for="logical_group_names" generated="true" class="error"></label></label>
                            <div id="logical-group-select" class="checkbox-select"></div>
                            <input id="action" name="action" value="insert" type="hidden"/>
                        </fieldset>
                    </form>
                </div>
                <div id="map-filters-dialog" title="Map Filters">
                    <div class="filters">
                        <div id="detector-filter">
                            <div class="string-filter-section">
                                <h2>Detector Text</h2>
                                <div><label>Detector Name:</label><input id="detector-table-filter-string-0" index="0" class="datatable-column-filter datatable-column-filter-value datatable-column-filter-string ui-corner-all ui-widget-content string-Detector"></div>
                                <div><label>ID:</label><input id="detector-table-filter-string-1" index="1" class="datatable-column-filter datatable-column-filter-value datatable-column-filter-string ui-corner-all ui-widget-content string-ID"></div>
                                <div><label>Location:</label><input id="detector-table-filter-string-2" index="2" class="datatable-column-filter datatable-column-filter-value datatable-column-filter-string ui-corner-all ui-widget-content string-Location"></div>
                                <div><label>MODE:</label><input id="detector-table-filter-string-5" index="5" class="datatable-column-filter datatable-column-filter-value datatable-column-filter-string ui-corner-all ui-widget-content string-MODE"></div>
                                <div><label>Carriageway:</label><input id="detector-table-filter-string-6" index="6" class="datatable-column-filter datatable-column-filter-value datatable-column-filter-string ui-corner-all ui-widget-content string-Carriageway"></div>
                                <div><label>Status:</label><input id="detector-table-filter-string-8" index="8" class="datatable-column-filter datatable-column-filter-value datatable-column-filter-string ui-corner-all ui-widget-content string-Status"></div>
                                <div><label>Logical Groups:</label><input id="detector-table-filter-string-7" index="7" class="datatable-column-filter datatable-column-filter-value datatable-column-filter-string ui-corner-all ui-widget-content string-LogicalGroups"></div>
                            </div>
                        </div>
                        <div id="span-filter">
                            <div class="string-filter-section">
                                <h2>Span/Route Text</h2>
                                <div><label>Span Name:</label><input id="span-table-filter-string-0" index="0" class="datatable-column-filter datatable-column-filter-value datatable-column-filter-string ui-corner-all ui-widget-content string-SpanName"></div>
                                <div><label>Start Detector ID:</label><input id="span-table-filter-string-3" index="3" class="datatable-column-filter datatable-column-filter-value datatable-column-filter-string ui-corner-all ui-widget-content string-Start"></div>
                                <div><label>End Detector ID:</label><input id="span-table-filter-string-4" index="4" class="datatable-column-filter datatable-column-filter-value datatable-column-filter-string ui-corner-all ui-widget-content string-End"></div>
                                <div><label>Status:</label><input id="detector-table-filter-string-5" index="5" class="datatable-column-filter datatable-column-filter-value datatable-column-filter-string ui-corner-all ui-widget-content string-Status"></div>
                                <div><label>Logical Groups:</label><input id="span-table-filter-string-11" index="11" class="datatable-column-filter datatable-column-filter-value datatable-column-filter-string ui-corner-all ui-widget-content string-LogicalGroups"></div>
                                <div><label>Routes:</label><input id="detector-table-filter-string-12" index="12" class="datatable-column-filter datatable-column-filter-value datatable-column-filter-string ui-corner-all ui-widget-content string-Routes"></div>
                            </div>
                            <div class="numeric-filter-section">
                                <h2>Span/Route Numeric</h2>
                                <div><label>Total Distance (Metres) (MIN):</label><input id="span-table-filter-numeric-min-2" index="2" class="datatable-column-filter datatable-column-filter-value datatable-column-filter-numeric-min ui-corner-all ui-widget-content numeric-min-TotalDistance"></div>
                                <div><label>Total Distance (Metres) (MAX):</label><input id="span-table-filter-numeric-max-2" index="2" class="datatable-column-filter datatable-column-filter-value datatable-column-filter-numeric-max ui-corner-all ui-widget-content numeric-max-TotalDistance"></div>
                            </div>
                        </div>
                    </div>
                </div>               
            </div>
            <%@include file="common/common-page-footer.jsp" %>
            </div>
    </body>
</html>
