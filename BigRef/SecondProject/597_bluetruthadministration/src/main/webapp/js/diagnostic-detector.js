function init_diagnostic_detector(current_detector_id, diagnostic_detail_role){
    display_diagnostic_data(current_detector_id);
    if(diagnostic_detail_role == "true"){
        create_detector_message_table(current_detector_id);
        create_detector_log_table(current_detector_id);
        $(".buttons button").button().bind("click", function(){
            display_diagnostic_data(current_detector_id);
            datatables['detector-message-table'].fnDraw(false);
            datatables['detector-log-table'].fnDraw(false);
        });
    } else {
        $(".buttons button").button().bind("click", function(){
            display_diagnostic_data(current_detector_id);
        });
    }
}

function display_diagnostic_data(current_detector_id){
    $("input", "#detector-diagnostic-data").val("-");
    $.post("DetectorDiagnosticManager",
    {
        "action":"get",
        "detector_id":current_detector_id
    },
    function(data){
        if(data.success){
            var diagnostic_data = data.data[0];
            $("#detector-diagnostic-data h1").text("Diagnostic Information for " +
                diagnostic_data.detector_name.value +":" +diagnostic_data.detector_id.value);
            $("#statistic-update-time").text("Diagnostic information requested at " + diagnostic_data.diagnostic_information_requested_timestamp.value.split(".")[0]);

            $("#detector-name-crumb").text(diagnostic_data.detector_name.value);
            $("#detector-id-crumb").text(diagnostic_data.detector_id.value);

            if(diagnostic_data.last_device_detection.value != 'null'){
                $("#last-device-detection").val(diagnostic_data.last_device_detection.value.split(".")[0]);
            } else {
                $("#last-device-detection").val("-");
            }
            if(diagnostic_data.total_device_detections.value != 'null'){
                $("#total-device-detections").val(diagnostic_data.total_device_detections.value);
            } else {
                $("#total-device-detections").val("-");
            }
            if(diagnostic_data.total_device_detections_last_5_minutes.value != 'null'){
                $("#total-device-detections-last-5-minutes").val(diagnostic_data.total_device_detections_last_5_minutes.value);
            } else {
                $("#total-device-detections-last-5-minutes").val("-");
            }

            if(diagnostic_data.last_occupancy_report_timestamp.value != 'null'){
                $("#last-occupancy-report").val(diagnostic_data.last_occupancy_report_timestamp.value.split(".")[0]);
            } else {
                $("#last-occupancy-report").val("-");
            }
            if(diagnostic_data.total_occupancy_reports.value != 'null'){
                $("#total-occupancy-reports").val(diagnostic_data.total_occupancy_reports.value);
            } else {
                $("#total-occupancy-reports").val("-");
            }

            if(diagnostic_data.last_configuration_download_request_timestamp.value != 'null'){
                $("#last-configuration-download-request").val(diagnostic_data.last_configuration_download_request_timestamp.value.split(".")[0]);
            } else {
                $("#last-configuration-download-request").val("-");
            }
            if(diagnostic_data.last_configuration_download_version.value != 'null'){
                $("#last-configuration-download-version").val(diagnostic_data.last_configuration_download_version.value);
            } else {
                $("#last-configuration-download-version").val("-");
            }
        }
    },
    "json");
}

function create_detector_message_table(current_detector_id){
    var table = $("#detector-message-table");
    var filter_div = $("<div/>");
    var table_headers = $("th", table);
    var detector_message_datatable_config = create_detector_message_datatable_config(filter_div, current_detector_id);
    datatables['detector-message-table'] = table.dataTable(detector_message_datatable_config);
    var datatable = datatables['detector-message-table'];
    
    $(".datatable-buttons", table.closest(".dataTables_wrapper"))
    .append(create_csv_download_button({
        filter_div:filter_div,
        datatable:datatable,
        parameters : [{"name":"detector_id", "value":current_detector_id}],
        ajax_url:detector_message_datatable_config.sAjaxSource
    }))
    .append(create_show_hide_column_button({
        dialog:$("#detector-message-show-hide-column-dialog"),
        headers:table_headers,
        datatable:datatable,
        aoColumnDefs:detector_message_datatable_config.aoColumnDefs
    }))
    .append(create_filter_button({
        dialog:$("#detector-message-filter-dialog"),
        filter_div:filter_div,
        datatable:datatable,
        datatable_headers:table_headers,
        column_filter_definitions:[{
            "target":0,
            "type":"string"
        },
        {
            "target":1,
            "type":"enum",
            "options": ["OK","INFORMATION","WARNING","CRITICAL"]
        },
        {
            "target":2,
            "type":"string"
        },
        {
            "target":3,
            "type":"string"
        },
        {
            "target":4,
            "type":"numeric"
        },
        {
            "target":5,
            "type":"date"
        }
        ]
    }));
}

function create_detector_message_datatable_config(filter_div, current_detector_id){
    var datatable_config = common_datatable_configuration();
    datatable_config.aoColumnDefs = [
    {
        "sClass":"text",
        "aTargets":[2,3]
    },
    {
        "sClass":"center",
        "aTargets":[0,1,4,5]
    }
    ];

    datatable_config.aoColumns = [
    null,
    null,
    null,
    null,
    null,
    {
        "mDataProp":function(source, type, val){
            return source[5].substring(0, 19);
        }
    }
    ];

    datatable_config.fnRowCallback = function(nRow, aData, iDisplayIndex){
        if (aData[1] == "OK"){
            $(nRow).addClass("status-ok");
        } else if (aData[1] == "INFORMATION"){
            $(nRow).addClass("status-information");
        } else if (aData[1] == "WARNING"){
            $(nRow).addClass("status-warning");
        } else if (aData[1] == "CRITICAL"){
            $(nRow).addClass("status-critical");
        }
    }

    datatable_config.aaSorting = [[5, "desc"]];

    datatable_config.sAjaxSource = "DetectorMessageDatatable";
    datatable_config.sDom = "<\"H\"l<\"datatable-buttons\">r>t<\"F\"ip>";
    datatable_config.fnServerData = get_datatable_request_function({
        detector_id:current_detector_id
    }, filter_div);

    return datatable_config;
}

function create_detector_log_table(current_detector_id){
    var table = $("#detector-log-table");
    var filter_div = $("<div/>");
    var table_headers = $("th", table);
    var detector_log_datatable_config = create_detector_log_datatable_config(filter_div, current_detector_id);
    datatables['detector-log-table'] = table.dataTable(detector_log_datatable_config);
    var datatable = datatables['detector-log-table'];

    $(".datatable-buttons", table.closest(".dataTables_wrapper"))
    .append(create_csv_download_button({
        filter_div:filter_div,
        datatable:datatable,
        parameters : [{"name":"detector_id", "value":current_detector_id}],
        ajax_url:detector_log_datatable_config.sAjaxSource
    }))
    .append(create_show_hide_column_button({
        dialog:$("#detector-log-show-hide-column-dialog"),
        headers:table_headers,
        datatable:datatable,
        aoColumnDefs:detector_log_datatable_config.aoColumnDefs
    }))
    .append(create_filter_button({
        dialog:$("#detector-log-filter-dialog"),
        filter_div:filter_div,
        datatable:datatable,
        datatable_headers:table_headers,
        column_filter_definitions:[
        {
            "target":1,
            "type":"date"
        }
        ]
    }));
}

function create_detector_log_datatable_config(filter_div, current_detector_id){
    var datatable_config = common_datatable_configuration();
    datatable_config.aoColumnDefs = [    
    {
        "sWidth":"100px",
        "aTargets":[2]
    },
    {
        "sClass":"center",
        "aTargets":[1,2]
    },
    {
        "aTargets": [0],
        "bVisible": false
    },
    {
        "bSortable":false,
        "aTargets":[2]
    }
    ];

    datatable_config.aoColumns = [
    null,
    {
        "mDataProp":function(source, type, val){
            return source[1].substring(0, 19);
        }
    },
    {
        "mDataProp":function(source, type, val){
            return "<a class='download' title='Download log ["+source[1].substring(0, 19)+"]' detectorlogid='"+source[0]+"'>download</a>";
        }
    }
    ];

    datatable_config.aaSorting = [[1, "desc"]];

    datatable_config.fnDrawCallback = function(){
        $('a.download', datatables['detector-log-table'])
        .button({
            icons:{
                primary:"ui-icon-arrowthick-1-s"
            }
        })
        .addClass("ui-state-highlight")
        .bind({
            click:function(){
                var id = $(this).attr("detectorlogid");
                if(id != undefined){
                    var file_location="/DetectorLogDownload?detector_log_id="+id;
                    window.location.href=file_location;
                }
            }
        });
    }

    datatable_config.aaSorting = [[1, "desc"]];

    datatable_config.sAjaxSource = "DetectorLogDatatable";
    datatable_config.sDom = "<\"H\"l<\"datatable-buttons\">r>t<\"F\"ip>";
    datatable_config.fnServerData = get_datatable_request_function({
        detector_id:current_detector_id
    }, filter_div);

    return datatable_config;
}