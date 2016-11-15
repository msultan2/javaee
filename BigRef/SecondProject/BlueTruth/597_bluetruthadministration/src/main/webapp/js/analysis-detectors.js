function init_analysis(){    
    create_analysis_detector_table();    
}

function create_analysis_detector_table(){
    var logical_group_index = 7;
    var recordType = "Detectors";
    var table = $("#detector-table");
    var filter_div = $("<div/>");
    var table_headers = $("th", table);
    var detector_datatable_config = create_detector_datatable_config(filter_div);
    datatables['detector-table'] = table.dataTable(detector_datatable_config);
    var datatable = datatables['detector-table'];


    var logical_group_options = [];
    $.post("CurrentUserManager",
    {
        action:"user"
    },
    function(data){
        logical_group_options = [];
        for(var i = 0; i <data.data.logical_groups.length; i++){
            logical_group_options.push("["+data.data.logical_groups[i]+"]");
        }
        // Quick filter
        create_quick_filter(table, logical_group_options, logical_group_index, recordType, table_headers);
        $(".datatable-buttons", table.closest(".dataTables_wrapper"))
        .append(create_csv_download_button({
            filter_div:filter_div,
            datatable:datatable,
            ajax_url:detector_datatable_config.sAjaxSource
        }))
        .append(create_show_hide_column_button({
            dialog:$("#detector-show-hide-column-dialog"),
            headers:table_headers,
            datatable:datatable,
            aoColumnDefs:detector_datatable_config.aoColumnDefs
        }))
        .append(create_filter_button({
            dialog:$("#detector-filter-dialog"),
            filter_div:filter_div,
            datatable:datatable,
            datatable_headers:table_headers,
            column_filter_definitions:[{
                "target":0,
                "type":"string"
            },
            {
                "target":1,
                "type":"string"
            },
            {
                "target":2,
                "type":"string"
            },
            {
                "target":3,
                "type":"numeric"
            },
            {
                "target":4,
                "type":"numeric"
            },
            {
                "target":5,
                "type":"string"
            },
            {
                "target":6,
                "type":"string"
            },
            {
                "target":logical_group_index,
                "type":"enum",
                "options": logical_group_options
            }
            ]
        }));

    },
    "json");
}

function create_detector_datatable_config(filter_div){
    var datatable_config = common_datatable_configuration();
    datatable_config.aoColumnDefs = [
    {
        "sClass":"center",
        "aTargets":[1,2,3,4,6,8]
    },
    {
        "bSortable":false,
        "aTargets":[7]
    },
    {
        "sWidth":"200px",
        "aTargets":[0,1]
    },
    {
        "sWidth":"200px",
        "aTargets":[5]
    },
    {
        "sWidth":"100px",
        "aTargets":[6]
    },
    {
        "sWidth":"140px",
        "aTargets":[8]
    },
    {
        "aTargets": [7],
        "bVisible": false
    }];

    datatable_config.aoColumns = [
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    {
        "mDataProp":function(source, type, val){
            return source[7].replace(/\] \[/g, "] <br/> [");
        }
    },
    {
        "mDataProp":function(source, type, val){
            return "<a class='view-graph' href='AnalysisDetectorDetections?detector_id="+source[1]+"&detector_name="+source[0]+"' title='View ["+source[0]+"] Detection Data' >View Detections</a>";
        }
    }
    ];

    datatable_config.fnDrawCallback = function(){
        $('a.view-graph', datatables['detector-table'])
        .button({
            icons:{
                primary:"ui-icon-search"
            }
        })
        .addClass("ui-state-highlight")
        .bind({
            click:function(){
                var detector_name = $(this).attr("detectorname");
                var detector_id = $(this).attr("detectorid");
                display_detector_graph_information(detector_name, detector_id);
            }
        });
    };

    datatable_config.sAjaxSource = "AnalysisDetectorsDatatable";
    datatable_config.sDom = "<\"H\"l<\"datatable-buttons\">r>t<\"F\"ip>";
    datatable_config.fnServerData = get_datatable_request_function({}, filter_div);

    return datatable_config;
}