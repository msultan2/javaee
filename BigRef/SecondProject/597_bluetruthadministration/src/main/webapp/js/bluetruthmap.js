function create_bluetruth_map(div_name) {
    var center_latitude = 51.383717;
    var center_longitude = -2.812468;

    initialise(div_name, center_latitude, center_longitude, 10);

//    var lat1 = 51.383717;
//    var lon1 = -2.812468;
//    var lat2 = 51.388217;
//    var lon2 = -2.822468;
//    var lat3 = 51.395717;
//    var lon3 = -2.832468;
//
//    addMapIcon("icon_1", "Sample Icon 1", lat1, lon1, "img/detector.png", 25, 25, 12, 12);
//    addMapIcon("icon_2", "Sample Icon 2", lat2, lon2, "img/detector.png", 25, 25, 12, 12);
//    addMapIcon("icon_3", "Sample Icon 3", lat3, lon3, "img/detector.png", 25, 25, 12, 12);
//    connectMapIcons("Sample Path 1", "icon_1", "icon_2");
//    connectMapIcons("Sample Path 2", "icon_2", "icon_3");
    
    $.post("MapDetectorDatatable", {}, _fn_handle_detector_response, "json");
}

function _fn_handle_detector_response(data) {
    if ($.isArray(data.aaData)) {
        for (var index = 0; index < data.aaData.length; index++) {
            var detectorData = data.aaData[index];
            
            addDetectorToMap(detectorData);
            
        }
    }
    
    $.post("MapSpanDatatable", {}, _fn_handle_span_response, "json");
}

function addDetectorToMap(detectorData) {
    var name = detectorData[0];
    var id = detectorData[1];

    var latitude = 0;
    var longitude = 0;

    if (detectorData[3] != null && detectorData[4] != null) {
        latitude = parseFloat(detectorData[3]);
        longitude = parseFloat(detectorData[4]);
        addMapIcon(id, latitude, longitude, "img/detector.png", 25, 25, 12, 12,
        {
            select: function(feature) {
                addPopup(feature, name + " - ID:" + id);
            },
            unselect: function(feature) {
                removePopup(feature);
            },
            unhover: function(feature) {
                removePopup(feature);
            }
        });
    }
}

function _fn_handle_span_response(data) {
    if ($.isArray(data.aaData)) {
        for (var index = 0; index < data.aaData.length; index++) {
            var spanData = data.aaData[index];
            
            var spanName = spanData[0];
            var startID = spanData[1];
            var endID = spanData[2];
            
            connectMapIcons(startID, endID, {
                unhover: function(feature) {
                    feature.style.strokeOpacity = 0.3;
                    vectorLayer.redraw();
                },
                hover: function(feature) {
                    feature.style.strokeOpacity = 1;
                    vectorLayer.redraw();
                }
            });
        }
    }
}