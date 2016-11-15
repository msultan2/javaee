var PERMISSIONS = {};
var map;
var mapURL = 'http://213.246.154.118/osm2/{z}/{x}/{y}.png';
var openstreamMapAttribute = 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, <a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>';
var MAP_MAX_ZOOM_LEVEL=16;
var maximumClusteringRadius = 100;
var disableClusteringAtZoom = 13;

var waypoint_layer = new L.FeatureGroup();
var span_feature_group_red = new L.FeatureGroup();
var span_feature_group_amber = new L.FeatureGroup();
var span_feature_group_green = new L.FeatureGroup();
var detector_markers_layer = null;

var span_data = [];
var update_displayed_elements_paused = false;
var detectorListForMapIndex=0;
var detectorListForMap = [];
var key=0;
var legend_green_desc = "Regular detections";
var legend_blue_desc = "Few detections";
var legend_grey_desc = "Low detections";


var clusterOptions ={
    maxClusterRadius: maximumClusteringRadius,
    spiderfyOnMaxZoom: true,
    showCoverageOnHover: true,
    zoomToBoundsOnClick: true,
    disableClusteringAtZoom: disableClusteringAtZoom,
    iconCreateFunction: function (cluster) {
        var html; 
        var classNameIcon;
        if ( $.browser.msie && $.browser.version <= 8) {
                html = '<div><span>' + cluster.getChildCount() + '</span></div>'; 
                classNameIcon = 'marker-cluster marker-cluster-custom-green';         
        } else {
            html = '<div><span>' + cluster.getChildCount() + '</span></div>';  
            classNameIcon = 'marker-cluster marker-cluster-small';
        }
        
        return new L.DivIcon({html: html,
            className: classNameIcon,
            iconSize: new L.Point(40, 40)});
    }
};

function initialiseMarkerClusterGroup(){
    if (detector_markers_layer === null) {
              detector_markers_layer = L.markerClusterGroup(clusterOptions);
    } 
}

var start_icon = L.icon({
    iconUrl:'/leaflet/images/start_marker.png',
    shadowUrl:'/leaflet/images/marker-shadow.png',
    iconSize: [32,37],
    iconAnchor: [16,36],
    shadowSize: [41,41],
    shadowAnchor: [15,40],
    popupAnchor: [0,-20]
});
var end_icon = L.icon({
    iconUrl:'/leaflet/images/end_marker.png',
    shadowUrl:'/leaflet/images/marker-shadow.png',
    iconSize: [32,37],
    iconAnchor: [16,36],
    shadowSize: [41,41],
    shadowAnchor: [15,40],
    popupAnchor: [0,-20]
});
var waypoint_icon = L.icon({
    iconUrl:'/leaflet/images/waypoint_marker.png',
    shadowUrl:'/leaflet/images/marker-shadow.png',
    iconSize: [32,37],
    iconAnchor: [16,36],
    shadowSize: [41,41],
    shadowAnchor: [15,40],
    popupAnchor: [0,-20]
});

var detector_green_icon = L.icon({
    iconUrl:'/leaflet/images/detector-green.png',
    shadowUrl:'/leaflet/images/marker-shadow.png',
    iconSize: [32,37],
    iconAnchor: [16,36],
    shadowSize: [41,41],
    shadowAnchor: [15,40],
    popupAnchor: [0,-20]
});
var detector_blue_icon = L.icon({
    iconUrl: '/leaflet/images/detector-blue.png',
    shadowUrl: '/leaflet/images/marker-shadow.png',
    iconSize: [32, 37],
    iconAnchor: [16, 36],
    shadowSize: [41, 41],
    shadowAnchor: [15, 40],
    popupAnchor: [0, -20]
});
var detector_red_icon = L.icon({
    iconUrl:'/leaflet/images/detector-red.png',
    shadowUrl:'/leaflet/images/marker-shadow.png',
    iconSize: [32,37],
    iconAnchor: [16,36],
    shadowSize: [41,41],
    shadowAnchor: [15,40],
    popupAnchor: [0,-20]
});
var detector_grey_icon = L.icon({
    iconUrl:'/leaflet/images/detector-grey.png',
    shadowUrl:'/leaflet/images/marker-shadow.png',
    iconSize: [32,37],
    iconAnchor: [16,36],
    shadowSize: [41,41],
    shadowAnchor: [15,40],
    popupAnchor: [0,-20]
});

function get_detector_icon(status){
    if(status === 'Silent'){
        return detector_grey_icon;
    } else if (status === 'Degraded') {
        return detector_blue_icon;
    } else {
        return detector_green_icon;
    }
}
var current_window_height;
var current_window_width;
function window_resize_watchdog(){
    var this_window = $(window);
    setInterval(function(){
        if(this_window.height() !== current_window_height || this_window.width() !== current_window_width){
            current_window_height = this_window.height();
            current_window_width = this_window.width();
            set_map_dimensions_and_position();
            map.invalidateSize(true);
            add_legend(current_window_height);
        }
    }, 500);
}

function set_map_dimensions_and_position(){
    //the map must be displayed directly below the common links bar
    var common_links = $("#common-links");
    var distance_from_top = common_links.offset().top + common_links.height();
    //Set the height to work with IE
    var map_height = $(window).height() - distance_from_top - 1;//1 = border width;
    $('#map-canvas').css({
        top:distance_from_top,
        height:map_height
    });
}

function add_map_controls(){
    var common_links = $("#common-links");
    var distance_from_top = common_links.offset().top + common_links.height() + 10;
    $("#map-controls").css({
        "position":"absolute",
        "top":distance_from_top,
        "right":"10px",
        "z-index":"10000"
    });
    $("#filters-button").button().bind({
        "click":display_filters_dialog
    });
    $("#layers-button").button().bind({
        "click":display_layers_options
    });    
    $("input","#layers-options").bind("change", function(){
        update_displayed_elements();
    });
    if(PERMISSIONS.detector_configuration){
        $("#tools-button").button().bind({
            "click":display_tools_options
        });
        $("#add-new-detector-button").button().bind({
            "click":start_add_new_detector_process
        });
        setup_add_new_detector_control();
        setup_edit_detector_location_control();
    } else {
        $("#tools-button").hide();
        $("#tools-button-label").hide();
    }
    if(PERMISSIONS.span_configuration){
        setup_edit_span_route_control();
    }
}

function setup_edit_detector_location_control(){
    var edit_detector_location_controls = $("#edit-detector-location-controls").hide();
    var common_links = $("#common-links");
    var distance_from_top = common_links.offset().top + common_links.height() + 10;
    var distance_from_right = ($(window).width() - edit_detector_location_controls.width())/2;
    edit_detector_location_controls.css({
        "position":"absolute",
        "top":distance_from_top,
        "right":distance_from_right,
        "z-index":"10000"
    }).hide();
    $("#edit-detector-location-confirm")
    .button()
    .bind("click",function(){
        if(edit_detector_marker !== undefined && edit_detector_marker_id !== undefined){
            var latlng = edit_detector_marker.getLatLng();
            $.post("DetectorManager",
            {
                action:"update-location",
                id:edit_detector_marker_id,
                latitude:latlng.lat,
                longitude:latlng.lng
            }, function(){
                map.off('click', move_marker_edit_detector_location);
                update_displayed_elements_paused = false;
                update_displayed_elements();
                edit_detector_location_controls.hide();
            });
        }
    });
    $("#edit-detector-location-cancel")
    .button()
    .bind("click",function(){
        map.off('click', move_marker_edit_detector_location);
        update_displayed_elements_paused = false;
        update_displayed_elements();
        edit_detector_location_controls.hide();        
    });
}

function setup_edit_span_route_control(){
    var edit_span_route_controls = $("#edit-span-route-controls").hide();
    var common_links = $("#common-links");
    var distance_from_top = common_links.offset().top + common_links.height() + 10;
    var distance_from_right = ($(window).width() - edit_span_route_controls.width())/2;
    edit_span_route_controls.css({
        "position":"absolute",
        "top":distance_from_top,
        "right":distance_from_right,
        "z-index":"10000"
    }).hide();
    $("#edit-span-route-confirm")
    .button()
    .bind("click",function(){
        $.post("SpanManager",
        {
            action:"update_span_osrm_client",
            span_name: span_data[0][0],
            route_geometry: span_data[0][1],
            total_distance: span_data[0][2],
            total_time: span_data[0][13]
        },
        function(data){ 
            if(data.success){
                waypoint_layer.clearLayers();
                map.off('click', create_waypoint_markers);
                update_displayed_elements_paused = false;
                update_displayed_elements();
                edit_span_route_controls.hide();
            }
        }, 
        "json");

    });
    $("#edit-span-route-cancel")
    .button()
    .bind("click",function(){
        waypoint_layer.clearLayers();
        map.off('click', create_waypoint_markers);
        update_displayed_elements_paused = false;
        update_displayed_elements();
        edit_span_route_controls.hide();
    });
}

function setup_add_new_detector_control(){
    var add_detector_controls = $("#add-detector-controls").hide();
    var common_links = $("#common-links");
    var distance_from_top = common_links.offset().top + common_links.height() + 10;
    var distance_from_right = ($(window).width() - add_detector_controls.width())/2;
    add_detector_controls.css({
        "position":"absolute",
        "top":distance_from_top,
        "right":distance_from_right,
        "z-index":"10000"
    }).hide();
    $("#add-detector-confirm")
    .button({
        "disabled":true
    })
    .bind("click",function(){
        map.off('click', add_marker_for_new_detector);
        add_detector_controls.hide();
        var latlng = add_detector_marker.getLatLng();
        $("#latitude").val(latlng.lat);
        $("#longitude").val(latlng.lng);
        display_new_detector_dialog();
    });
    $("#add-detector-cancel")
    .button()
    .bind("click",function(){
        map.off('click', add_marker_for_new_detector);
        update_displayed_elements_paused = false;
        update_displayed_elements();
        add_detector_controls.hide();
        $("#add-detector-confirm").button({
            "disabled":true
        });
    });
}

function display_new_detector_dialog(){
    $.post("CurrentUserManager",
    {
        action:"visible_logical_groups"
    },
    function(data){
        $("#logical-group-select").empty();
        for(var i = 0; i <data.data.length; i++){
            $("#logical-group-select").append($("<div/>")
                .append($("<label/>").text(data.data[i]))
                .append($("<input type='checkbox' checked='checked'>")
                    .val(data.data[i])
                    .attr("name","logical_group_names")
                    .addClass("required"))
                );
        }
        $("#logical-group-select").append($("<hr/>").addClass("hr-clear-float"));
    },
    "json");

    $(".message").text("").removeClass("error");

    var dialog = $("#new-detector-dialog");
    dialog.dialog({
        modal:true,
        width: "600px",
        buttons: {
            "Add new detector": function(){
                var form = dialog.find("form");

                form.validate({
                    messages: {
                        logical_group_names: {
                            required: "At least one logical group must be selected."
                        }
                    }
                });

                if(form.valid()){
                    $.post("DetectorManager",
                        form.serialize(),
                        function(data){
                            if(data.success){
                                dialog.dialog("close");
                                map.off('click', add_marker_for_new_detector);
                                update_displayed_elements_paused = false;
                                update_displayed_elements();
                                $("#add-detector-controls").hide();
                                $("#add-detector-confirm").button({
                                    "disabled":true
                                });
                            } else {
                                $(".message").text(data.message).addClass("error");
                            }
                        },
                        "json");
                }
            },
            "Cancel": function(){
                map.off('click', add_marker_for_new_detector);
                update_displayed_elements_paused = false;
                update_displayed_elements();
                $("#add-detector-controls").hide();
                $("#add-detector-confirm").button({
                    "disabled":true
                });
                dialog.dialog("close");
            }
        }
    });
}

var start_span_route_marker;
var end_span_route_marker;
function start_edit_span_route_process(event){
    waypoints = {};
    map.closePopup();
    start_span_route_marker = undefined;
    end_span_route_marker = undefined;
    var edit_span_route_controls = $("#edit-span-route-controls");
    edit_span_route_controls.show();

    //Stop redraw map layer action from taking place
    update_displayed_elements_paused = true;

    //Remove all layers from map
    detector_markers_layer.clearLayers();
    span_feature_group_red.clearLayers();
    span_feature_group_amber.clearLayers();
    span_feature_group_green.clearLayers();

    event.data.spanData[5] = "edit-route";
    span_data = [event.data.spanData];

    var line_points = decodeLine(event.data.spanData[1]);

    start_span_route_marker = L.marker(line_points[0],
    {
        icon:start_icon,
        draggable: true
    }).on("dragend", draw_new_span_route);
    
    detector_markers_layer.addLayer(start_span_route_marker);
    
    end_span_route_marker = L.marker(line_points[line_points.length-1],
    {
        icon:end_icon,
        draggable: true
    }).on("dragend", draw_new_span_route);
    
    detector_markers_layer.addLayer(end_span_route_marker);
    
    draw_spans();

    //Add onclick event that adds a marker to the map
    map.on('click', create_waypoint_markers);
}

var waypoints = {};
function create_waypoint_markers(event){
    var count = 0;
    for(w in waypoints){
        count++;
    }
    if(count < 5){
        var waypoint = L.marker([event.latlng.lat, event.latlng.lng],
        {
            icon:waypoint_icon,
            draggable: true
        });
        waypoint.on({
            "click":function(){
                delete waypoints[this.id];
                waypoint_layer.removeLayer(this);
                draw_new_span_route();
            },
            "dragend": function(event){
                //    alert(waypoints[this.id].getLatLng().lat);
                draw_new_span_route();
            }
        });

        waypoint.id = Math.random();
        waypoints[waypoint.id] = waypoint;

        waypoint_layer.clearLayers();
        for(w in waypoints){
            waypoint_layer.addLayer(waypoints[w]);
        }

        draw_new_span_route();
    }
}

function draw_new_span_route(){
    jQuery.ajaxSettings.traditional = true;
    var start = start_span_route_marker.getLatLng();
    var end = end_span_route_marker.getLatLng();

    var waypoint_params = "";

    for(w in waypoints){
        var latlng = waypoints[w].getLatLng();
        waypoint_params += "&loc="+latlng.lat +","+ latlng.lng
    }
    
    var url = "http://213.246.154.118/viaroute?z=15&output=json"+
                "&loc="+start.lat +","+ start.lng+
                waypoint_params+
                "&loc="+end.lat +","+ end.lng;

    $.ajax({
        type:"GET",
        url:url,
        dataType:"jsonp",
        jsonp:"jsonp",
        jsonpCallback:"osrm_route_information",
        cache:true
    });
}

function osrm_route_information(data){
    span_feature_group_red.clearLayers();
    span_feature_group_amber.clearLayers();
    span_feature_group_green.clearLayers();
    if(data.route_geometry !== ""){
        span_data[0][1] = data.route_geometry;
        span_data[0][2] = data.route_summary.total_distance;
        span_data[0][5] = "edit-route";
        span_data[0][13] = data.route_summary.total_time;

        draw_spans();
    }
}


var edit_detector_marker;
var edit_detector_marker_id;
function start_edit_detector_location_process(event){
    edit_detector_marker = undefined;
    edit_detector_marker_id = undefined;
    var edit_detector_location_controls = $("#edit-detector-location-controls");
    edit_detector_location_controls.show();

    //Stop redraw map layer action from taking place
    update_displayed_elements_paused = true;

    //Remove all layers from map
    detector_markers_layer.clearLayers();
    span_feature_group_red.clearLayers();
    span_feature_group_amber.clearLayers();
    span_feature_group_green.clearLayers();
    //Add onclick event that adds a marker to the map
    edit_detector_marker = L.marker([event.data.lat, event.data.lng],
    {
        icon:get_detector_icon(event.data.status),
        draggable: true
    });
    edit_detector_marker_id = event.data.id;
    detector_markers_layer.addLayer(edit_detector_marker);
    map.on('click', move_marker_edit_detector_location);
}
function move_marker_edit_detector_location(e){
    edit_detector_marker.setLatLng(e.latlng);
}

function start_add_new_detector_process(){
    $("#detector-layer").attr("checked","checked")
    $("#tools-options").hide();
    $("#tools-button").attr("checked", false).button("refresh");
    var add_detector_controls = $("#add-detector-controls");
    add_detector_controls.show();

    //Stop redraw map layer action from taking place
    update_displayed_elements_paused = true;

    //Remove all layers from map
    detector_markers_layer.clearLayers();
    span_feature_group_red.clearLayers();
    span_feature_group_amber.clearLayers();
    span_feature_group_green.clearLayers();
    //Add onclick event that adds a marker to the map
    map.on('click', add_marker_for_new_detector);
}
var add_detector_marker;
function add_marker_for_new_detector(e){
    detector_markers_layer.clearLayers();
    add_detector_marker = L.marker(e.latlng,
    {
        icon:get_detector_icon(),
        draggable: true
    });
    detector_markers_layer.addLayer(add_detector_marker);
    $("#add-detector-confirm").button({
        "disabled":false
    });
}

function display_layers_options(){
    var options = $("#layers-options");
    if(options.is(":visible")){
        $("#layers-options").hide();
        $("#layers-button").attr("checked", false).button("refresh");
    }else {
        $("#layers-options").show();
        $("#layers-button").attr("checked", true).button("refresh");
    }
    
}

function display_tools_options(){
    var options = $("#tools-options");
    if(options.is(":visible")){
        $("#tools-options").hide();
        $("#tools-button").attr("checked", false).button("refresh");
    }else {
        $("#tools-options").show();
        $("#tools-button").attr("checked", true).button("refresh");
    }
}

function display_filters_dialog(){
    $("#filters-button").attr("checked", false).button("refresh");
    var dialog = $("#map-filters-dialog");

    if(dialog.is(":visible")){
        dialog.dialog("close");
    } else {
        dialog.dialog({
            width: "700px",
            buttons: {
                "Refresh": function(){
                    update_displayed_elements();
                },
                "Close": function(){
                    dialog.dialog("close");
                }
            }
        });
    }
}

function init_map(config){
    PERMISSIONS = config.permissions;
    add_map_controls();
    set_map_dimensions_and_position();
    window_resize_watchdog();
    initialiseMarkerClusterGroup(); 
    setup_map(function(e){
        update_displayed_elements();
    });
    set_current_user_visible_map();    
    define_overlays();
    update_displayed_elements();
    map.on("popupopen", function(evt){
        currentPopup = evt.popup
    });
}


var mapZoomOutForDetectors = 5;
var mapZoomOutForSpans = 7;
var spansDataBatchCount = 50;
var detectorsDataBatchCount = 50;
var markerClusterStatus = true;

function update_displayed_elements(){
    
    if(update_displayed_elements_paused){
        return;
    }

    if(map.getZoom() < mapZoomOutForDetectors){
        detector_markers_layer.clearLayers(); 
        span_feature_group_red.clearLayers();
        span_feature_group_amber.clearLayers();
        span_feature_group_green.clearLayers();
    } else {
        
        if ($("#clustering-layer").is(":checked")) {
            if(markerClusterStatus === false){
                detector_markers_layer.clearLayers(); 
                map.removeLayer(detector_markers_layer);
                detector_markers_layer = null;
                initialiseMarkerClusterGroup();
                map.addLayer(detector_markers_layer);
            }
            markerClusterStatus = true;
        } else {
            if(markerClusterStatus === true){
                detector_markers_layer.clearLayers(); 
                map.removeLayer(detector_markers_layer);
                detector_markers_layer = new L.FeatureGroup();
                map.addLayer(detector_markers_layer);
            }
            markerClusterStatus = false;
        }

        var latLngBounds = map.getBounds();

        var southwest = latLngBounds.getSouthWest();
        var northeast = latLngBounds.getNorthEast();

        if ($("#detector-layer").is(":checked")) {
            
            var detectors_additonal_params = {
                "bFilterFlag_3":true,
                "sFilterMin_3":southwest.lat,
                "sFilterMax_3":northeast.lat,
                "bFilterMinIncl_3":southwest.lat,
                "bFilterMaxIncl_3":northeast.lat,
                "bFilterFlag_4":true,
                "sFilterMin_4":southwest.lng,
                "sFilterMax_4":northeast.lng,
                "bFilterMinIncl_4":southwest.lng,
                "bFilterMaxIncl_4":northeast.lng,
                "iSortCol_0": '4',
                "sSortDir_0": 'asc'
            };

            $("input.datatable-column-filter-string", "#detector-filter").each(function(){
                var value = $(this).val();
                if(value !== ""){
                    detectors_additonal_params["bFilterFlag_"+$(this).attr("index")] = true;
                    detectors_additonal_params["sSearch_"+$(this).attr("index")] = value;
                }
            });
            $("input.datatable-column-filter-numeric-min", "#detector-filter").each(function(){
                var value = $(this).val();
                if(value !== ""){
                    detectors_additonal_params["bFilterFlag_"+$(this).attr("index")] = true;
                    detectors_additonal_params["bFilterMin_"+$(this).attr("index")] = value;
                    detectors_additonal_params["bFilterMinIncl_"+$(this).attr("index")] = value;
                }
            });
            
            $.post("MapDetectorDatatable",
                detectors_additonal_params,
                _fn_handle_detector_response, "json");
            	$("#legend").show();
        } else {
            detector_markers_layer.clearLayers();
            $("#legend").hide();
        }

        if(($("#span-layer").is(":checked") || $("#route-layer").is(":checked")) && map.getZoom() > mapZoomOutForSpans){
            if(drawing_spans){
        
            } else {
                var spans_additonal_params = {
                    "northeastlat":northeast.lat,
                    "northeastlng":northeast.lng,
                    "southwestlat":southwest.lat,
                    "southwestlng":southwest.lng
                };
                if($("#span-layer").is(":checked")){
                    spans_additonal_params.span_layer = "span_layer";
                }
                if($("#route-layer").is(":checked")){
                    spans_additonal_params.route_layer = "route_layer";
                }
                $("input.datatable-column-filter-string", "#span-filter").each(function(){
                    var value = $(this).val();
                    if(value !== ""){
                        spans_additonal_params["bFilterFlag_"+$(this).attr("index")] = true;
                        spans_additonal_params["sSearch_"+$(this).attr("index")] = value;
                    }
                });
                $("input.datatable-column-filter-numeric-min", "#span-filter").each(function(){
                    var value = $(this).val();
                    if(value !== ""){
                        spans_additonal_params["bFilterFlag_"+$(this).attr("index")] = true;
                        spans_additonal_params["sFilterMin_"+$(this).attr("index")] = value;
                        spans_additonal_params["bFilterMinIncl_"+$(this).attr("index")] = true;
                    }
                });
                $("input.datatable-column-filter-numeric-max", "#span-filter").each(function(){
                    var value = $(this).val();
                    if(value !== ""){
                        spans_additonal_params["bFilterFlag_"+$(this).attr("index")] = true;
                        spans_additonal_params["sFilterMax_"+$(this).attr("index")] = value;
                        spans_additonal_params["bFilterMaxIncl_"+$(this).attr("index")] = true;
                    }
                });
                drawing_spans = true;

                $.post("MapSpanDatatable",                
                    spans_additonal_params,
                    _fn_handle_span_response, "json");
            }
        } else {
            span_feature_group_red.clearLayers();
            span_feature_group_amber.clearLayers();
            span_feature_group_green.clearLayers();
        }
    }
}

function define_overlays(){
    map.removeLayer(detector_markers_layer);
    map.addLayer(detector_markers_layer);
    map.removeLayer(waypoint_layer);
    map.addLayer(waypoint_layer);
    map.removeLayer(span_feature_group_green);
    map.addLayer(span_feature_group_green);
    map.removeLayer(span_feature_group_amber);
    map.addLayer(span_feature_group_amber);
    map.removeLayer(span_feature_group_red);
    map.addLayer(span_feature_group_red);
    
    //L.control.layers(null, overlays).addTo(map);
    L.control.scale().addTo(map);
}

function set_current_user_visible_map(){
    $.post("MapManager", {
        "action":"bounds"
    }, function(data){
        map.fitBounds(L.latLngBounds(new L.LatLng(data.data.south_east_lat, data.data.south_east_lng), new L.LatLng(data.data.north_west_lat, data.data.north_west_lng)));
    }, "json");
}

function setup_map(map_element_redraw_callback){
    map = L.map('map-canvas')
    .setView([54, -4], 6)
    .on({
        moveend:map_element_redraw_callback
    });
    L.tileLayer(mapURL, {
        attribution: openstreamMapAttribute,
        maxZoom: MAP_MAX_ZOOM_LEVEL
    }).addTo(map);
}

function get_detector_popup_html(name, id, lat, lng, location, mode, status, carriageway, logical_groups){
    var popup = 
    $("<div/>").addClass("detector-popup")
    .append($("<div/>").addClass("detector-name").text(name))
    .append($("<hr/>"))
    .append($("<div/>").addClass("row")
        .append($("<div/>").text("ID:").addClass("key"))
        .append($("<div/>").text(id))
        ).append($("<div/>").addClass("row")
        .append($("<div/>").text("location:").addClass("key"))
        .append($("<div/>").text(location))
        ).append($("<div/>").addClass("row")
        .append($("<div/>").text("carriageway:").addClass("key"))
        .append($("<div/>").text(carriageway))
        )
    .append($("<div/>").addClass("row")
        .append($("<div/>").text("latitude:").addClass("key"))
        .append($("<div/>").text(lat))
        ).append($("<div/>").addClass("row")
        .append($("<div/>").text("longitude:").addClass("key"))
        .append($("<div/>").text(lng))
        ).append($("<div/>").addClass("row")
        .append($("<div/>").text("mode:").addClass("key"))
        .append($("<div/>").text(mode))
        ).append($("<div/>").addClass("row")
        .append($("<div/>").text("status:").addClass("key"))
        .append($("<div/>").text(status))
        ).append($("<div/>").addClass("row")
        .append($("<div/>").text("groups:").addClass("key"))
        .append($("<div/>").text(logical_groups))
        );
    if(PERMISSIONS.detector_configuration || PERMISSIONS.analysis || PERMISSIONS.diagnostic){
        popup.append($("<hr/>"));
    }
    if(PERMISSIONS.detector_configuration){
        popup.append($("<div/>").addClass("row")
            .append($("<a/>").attr("href","Detector?detectorId="+id+"&detectorName="+name).attr("target","_blank").text("Additional Information"))
            );
        popup.append($("<div/>").addClass("row")
            .append($("<a/>").attr("href","DetectorConfiguration?id="+id).attr("target","_blank").text("Configuration"))
            );
    }
    if(PERMISSIONS.diagnostic){
        popup.append($("<div/>").addClass("row")
            .append($("<a/>").attr("href","DiagnosticDetector?id="+id).attr("target","_blank").text("Diagnostic"))
            );
    }
    if(PERMISSIONS.analysis){
        popup.append($("<div/>").addClass("row")
            .append($("<a/>").attr("href","AnalysisDetectorDetections?detector_id="+id+"&detector_name="+name).attr("target","_blank").text("Analysis"))
            );
    }
    if(PERMISSIONS.detector_configuration || PERMISSIONS.analysis || PERMISSIONS.diagnostic){
        popup.append($("<hr/>"));
    }
    if(PERMISSIONS.detector_configuration){
        popup.append($("<div/>").addClass("row")
            .append($("<button/>").button().addClass("popup-button")
                .text("Edit Detector Location")
                .bind("click", {
                    id:id,
                    lat:lat,
                    lng:lng,
                    status:status
                }, start_edit_detector_location_process))
            );
    }

    return popup[0];
}

//Handle detector response
function _fn_handle_detector_response(data) {
    detector_markers_layer.clearLayers(); 
    detectorListForMap = data;   
    plotDetectors();
}

//Update Clustering to load detectors faster using locking.
function plotDetectors() {
    detectorListForMapIndex=0;
    key++;
    plotDetectorMarkers(key);
}

//Update Map to load detectors faster using locking (Improve Performance as for every request currently the map is getting data from the backend)
function plotDetectorMarkers(localKey) {   
    
    var detectorProcessed = 0;
    
    if(detectorListForMap && $.isArray(detectorListForMap.aaData) && detectorListForMap.aaData.length > 0){
        
        for(;detectorListForMapIndex < detectorListForMap.aaData.length; detectorListForMapIndex++){
        
            if (key !== localKey) {
               return;
            }
        
            if (detectorProcessed++ >= detectorsDataBatchCount) {
                setTimeout(function() {
                    plotDetectorMarkers(localKey);
                }, 0);
                return;
            }
            
            var detectorData = detectorListForMap.aaData[detectorListForMapIndex];
            var name = detectorData[0];
            var id = detectorData[1];
            var location = detectorData[2];
            var latitude = 0;
            var longitude = 0;
            var mode = "";
            var status = "";
            var carriageway = "";
            var logical_groups = "";
            if (detectorData[3] !== null && detectorData[4] !== null) {
                latitude = parseFloat(detectorData[3]);
                longitude = parseFloat(detectorData[4]);
                mode = detectorData[5];                
                carriageway = detectorData[6];
                logical_groups = detectorData[8];
                status = detectorData[9];

                var marker =  L.marker([latitude,longitude],
                {
                    icon:get_detector_icon(detectorData[9])
                })
                .bindPopup(get_detector_popup_html(name,id,latitude,longitude,location,mode,status,carriageway,logical_groups),{
                        autoPanPadding:false,
                        closeButton:false,
                        maxWidth:350
                });                
                
                detector_markers_layer.addLayer(marker);    
            }
        }
    }
}

function _fn_handle_span_response(data) {    
    if ($.isArray(data.aaData)) {        
        span_data = data.aaData;
        draw_spans();
    } else {
        drawing_spans = false;
    }
}

function get_span_popup_html(name, distance, start_detector, end_detector, status, speed, speed_category, logical_groups, routes, spanData){

    speed = speed + " MPH " +"("+speed_category+")";
    if(status === "Silent"){
        speed = " - ";
    }
  
    var span_popup = $("<div/>").addClass("detector-popup")
    .append($("<div/>").addClass("detector-name").text(name))
    .append($("<hr/>"))
    .append($("<div/>").addClass("row")
        .append($("<div/>").text("distance:").addClass("key"))
        .append($("<div/>").text(distance+"m")))
    .append($("<div/>").addClass("row")
        .append($("<div/>").text("start detector:").addClass("key"))
        .append($("<div/>").text(start_detector)))
    .append($("<div/>").addClass("row")
        .append($("<div/>").text("end detector:").addClass("key"))
        .append($("<div/>").text(end_detector)))
    .append($("<div/>").addClass("row")
        .append($("<div/>").text("status:").addClass("key"))
        .append($("<div/>").text(status)))
    .append($("<div/>").addClass("row")
        .append($("<div/>").text("average speed:").addClass("key"))
        .append($("<div/>").text(speed)));

    if(logical_groups !== undefined){
        span_popup.append($("<div/>").addClass("row")
            .append($("<div/>").text("groups:").addClass("key"))
            .append($("<div/>").text(logical_groups)));
    }
    if(routes !== undefined){    
        span_popup.append($("<div/>").addClass("row")
            .append($("<div/>").text("routes:").addClass("key"))
            .append($("<div/>").text(routes)));
    }

    if(PERMISSIONS.span_configuration || PERMISSIONS.analysis || PERMISSIONS.diagnostic){
        span_popup.append($("<hr/>"));
    }
    if(PERMISSIONS.span_configuration){
        span_popup.append($("<div/>").addClass("row")
            .append($("<a/>").attr("href","Span?spanName="+name).attr("target","_blank").text("Additional Information")));
    }
    if(PERMISSIONS.analysis){
        span_popup.append($("<div/>").addClass("row")
            .append($("<a/>").attr("href","AnalysisSpanDuration?span="+name).attr("target","_blank").text("Duration Analysis")));
        span_popup.append($("<div/>").addClass("row")
            .append($("<a/>").attr("href","AnalysisSpanSpeed?span="+name).attr("target","_blank").text("Speed Analysis")));
    }

    if(PERMISSIONS.detector_configuration){
        span_popup.append($("<hr/>")).append($("<div/>").addClass("row")
            .append($("<button/>").button().addClass("popup-button")
                .text("Edit Span Route")
                .bind("click", {
                    span_name:name,
                    spanData:spanData
                }, start_edit_span_route_process))
            );
    }

    return span_popup[0];
}

var drawing_spans = false;
var span_key = 0;
var span_map_index=0;

function draw_spans(){
    span_feature_group_red.clearLayers();
    span_feature_group_amber.clearLayers();
    span_feature_group_green.clearLayers();    
    span_map_index=0;
    span_key++;
    plot_spans(span_key);    
}

function plot_spans(local_key){
    
    var spanProcessed = 0;
    
    if(span_data && $.isArray(span_data) && span_data.length > 0){        
        
        for(;span_map_index < span_data.length; span_map_index++ ){            
        
            //span_key is the global key and will increment after every 50 spans are processed.
            if(span_key !== local_key){
                return;
            }
            
            //span processed data will timeout and start again to improve performance.
            if(spanProcessed++ >= spansDataBatchCount){
                setTimeout(function (){
                    plot_spans(local_key);
                }, 0);
                return;
            }
            
            plotSpanPolyLines(span_data[span_map_index]);
            
        }
    }    
}

function plotSpanPolyLines(spanData){
    
    var magnitude = 1;
    var zoom = 15;
    var spanName = spanData[0];
    var route_geometry = spanData[1];
    var total_distance = spanData[2];
    var startID = spanData[3];
    var endID = spanData[4];
    var status = spanData[5];
    var speed = spanData[6].split(".")[0];
    
    if(route_geometry !== null || route_geometry !== 'null'){
        var speed_stationary = spanData[7];
        var speed_very_slow = spanData[8];
        var speed_slow = spanData[9];
        var speed_moderate = spanData[10];

        var logical_groups;
        if(spanData[11] !== null && spanData[11] !== ""){
            logical_groups = spanData[11];
        }
        var routes;
        if(spanData[12] !== null && spanData[12] !== ""){
            routes = spanData[12];
        }

        //Default Speed Category
        var speed_category = "free flow";
        //Default Free Flow Colour is Green
        var line_color = '#009900';
        
        if($("input:checked","#span-color-options").val() === "speed"){
            if(parseInt(speed) < parseInt(speed_stationary)){
                speed_category = "stationary";
                line_color = '#990000';
            } else if(parseInt(speed) < parseInt(speed_very_slow)){
                speed_category = "very slow";
                line_color = '#990000';
            } else if(parseInt(speed) < parseInt(speed_slow)){
                speed_category = "slow";
                line_color = '#E68426';
            }else if(parseInt(speed) < parseInt(speed_moderate)){
                speed_category = "moderate";
                line_color = '#E68426';
            }
        } else {
            //Reporting Status
            if(status === 'Degraded'){
                line_color = '#E68426';
            } 
        }

        //If a span is silent override speed
        if(status === 'Silent'){
            line_color = '#777';
        }
        //If a span is editing override everything
        if(status === "edit-route"){
            line_color = '#0000FF';
        }
            
        var polyline = L.polyline(
            transform_points_into_left_carriageway(
                L.LineUtil.simplify(decodeLine(route_geometry)),
                magnitude*5, zoom),
                {
                    color: line_color,
                    weight: 7,
                    opacity: 1.0
                });
                
        if(status !== "edit-route"){                            
            polyline.bindPopup(get_span_popup_html(spanName, total_distance, startID, endID, status, speed, speed_category, logical_groups, routes, spanData),            {
                autoPanPadding:false,
                closeButton:false,
                maxWidth:350
            });            
        }

        if(line_color === '#990000'){
            span_feature_group_red.addLayer(polyline);
        } else if(line_color === '#E6B426'){
            span_feature_group_amber.addLayer(polyline);
        } else {
            span_feature_group_green.addLayer(polyline);
        }
    }
    drawing_spans = false;
}

function zoomToFeature(e) {
    map.fitBounds(e.target.getBounds());
}

function decodeLine(encoded) {
    var len = encoded.length;
    var index = 0;
    var array = [];
    var lat = 0;
    var lng = 0;
    while (index < len) {
        var b;
        var shift = 0;
        var result = 0;
        do {
            b = encoded.charCodeAt(index++) - 63;
            result |= (b & 0x1f) << shift;
            shift += 5;
        } while (b >= 0x20);
        var dlat = ((result & 1) ? ~(result >> 1) : (result >> 1));
        lat += dlat;
        shift = 0;
        result = 0;
        do {
            b = encoded.charCodeAt(index++) - 63;
            result |= (b & 0x1f) << shift;
            shift += 5;
        } while (b >= 0x20);
        var dlng = ((result & 1) ? ~(result >> 1) : (result >> 1));
        lng += dlng;
        array.push(new L.LatLng(lat * 1e-5, lng * 1e-5));
    }
    return array;
}

function transform_points_into_left_carriageway(points, magnitude, zoom_level){
    var transformed_points = [];
    //Hold these variables so that each iteration can average with the previous.
    var previous_norm_dx, previous_norm_dy;
    var norm_dx, norm_dy;
    for(var i = 1; i< points.length; i++){
        var p1 = map.project(points[i-1], zoom_level);
        var p2 = map.project(points[i], zoom_level);
        if(p1.x != p2.x || p1.y != p2.y){
            var x1 = p1.x;
            var x2 = p2.x;
            var y1 = p1.y;
            var y2 = p2.y;
            var dx = x2 - x1;
            var dy = y2 - y1;
            var hyp = Math.sqrt(dx*dx + dy*dy);
            norm_dx = dx/hyp;
            norm_dy = dy/hyp;
            var averaged_norm_dx = norm_dx;
            var averaged_norm_dy = norm_dy;
            if(i>1){
                var p3 = map.project(points[i-2], zoom_level);
                if(p2.x != p3.x || p2.y != p3.y){
                    averaged_norm_dx = (norm_dx + previous_norm_dx);
                    averaged_norm_dy = (norm_dy + previous_norm_dy);
                    var avg_hyp = Math.sqrt(averaged_norm_dx*averaged_norm_dx + averaged_norm_dy*averaged_norm_dy);
                    averaged_norm_dx /= avg_hyp;
                    averaged_norm_dy /= avg_hyp;
                    transformed_points.push(map.unproject(transform_point(x1, y1, averaged_norm_dx, averaged_norm_dy, magnitude), zoom_level));
                }
            } else {
                transformed_points.push(map.unproject(transform_point(x1, y1, norm_dx, norm_dy, magnitude), zoom_level));
            }
            previous_norm_dx = norm_dx;
            previous_norm_dy = norm_dy;
        }
    }
    //Finally, add the last normalized point without averaging.
    transformed_points.push(map.unproject(transform_point(x2, y2, norm_dx, norm_dy, magnitude), zoom_level));
    return transformed_points;
}

function transform_point(x, y, dx, dy, magnitude){
    return new L.Point(x+dy*magnitude, y-dx*magnitude);
}

function add_legend(window_height) {
    var legendHeight = $('#legend').height();
    $('#legend').offset({top: window_height - legendHeight - 20});
    $('#legend-green').text(legend_green_desc);
    $('#legend-blue').text(legend_blue_desc);
    $('#legend-grey').text(legend_grey_desc);
}
