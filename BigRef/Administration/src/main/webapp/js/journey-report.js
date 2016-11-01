function init_journey_reports(){

    var table = $("#journey-time-table");
    var logical_group_index = 6;
    var recordType = "Journey Times";
    var filter_div = $("<div/>");
    var table_headers = $("th", table);
    var journey_time_datatable_config = create_journey_time_datatable_config(filter_div);
    datatables['journey-time-table'] = table.dataTable(journey_time_datatable_config);
    var datatable = datatables['journey-time-table'];

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
            ajax_url:journey_time_datatable_config.sAjaxSource
        }))
        .append(create_show_hide_column_button({
            dialog:$("#journey-time-show-hide-column-dialog"),
            headers:table_headers,
            datatable:datatable,
            aoColumnDefs:journey_time_datatable_config.aoColumnDefs
        }))
        .append(create_filter_button({
            dialog:$("#journey-time-filter-dialog"),
            filter_div:filter_div,
            logical_group_index:logical_group_index,
            recordType:recordType,
            datatable:datatable,
            datatable_headers:table_headers,
            column_filter_definitions:[{
                "target":0,
                "type":"string"
            },
            {
                "target":1,
                "type":"time"
            },
            {
                "target":2,
                "type":"integer"
            },
            {
                "target":3,
                "type":"integer"
            },
            {
                "target":4,
                "type":"string"
            },
            {
                "target":5,
                "type":"date"
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

    start_datatable_refresh_interval();
}

function create_journey_time_datatable_config(filter_div){
    var datatable_config = common_datatable_configuration();    
    datatable_config.aoColumnDefs = [    
    {
        "sClass":"center",
        "aTargets":[1,2,3,5]
    },
    {
        "sClass":"status center",
        "aTargets":[4]
    },
    {
        "aTargets": [-1],
        "bVisible": false
    }
    ];

//    datatable_config.fnRowCallback = function(nRow, aData, iDisplayIndex){
//        if (aData[1] == "-" || aData[2] == 0 || aData[3] != aData[4]){
//            $(nRow).addClass("reporting-problem");
//        }
//    }
    
    datatable_config.iDisplayLength = 25;

    datatable_config.aaSorting = [[0,"asc"]];

    datatable_config.aoColumns = [
    null,
    {
        "mDataProp":function(source, type, val){
            if(source[4] == "Silent"){
                return "-";
            } else {
                return format_interval_string(source[1]);
            }
        }
    },
    {
        "mDataProp":function(source, type, val){
            if(source[4] == "Silent"){
                return "-";
            } else {
                return source[2].split(".")[0] + " MPH";
            }
        }
    },    
    {
        "mDataProp":function(source, type, val){
            if(source[4] == "Silent"){
                return "-";
            } else {
                return source[3];
            }
        }
    },
    null,
    {
        "mDataProp":function(source, type, val){
            if(source[4] == "Silent"){
                return "-";
            } else {
                return source[5].substring(0, 19);
            }
        }
    },
    {
        "mDataProp":function(source, type, val){
            return source[6].replace(/\] \[/g, "] <br/> [");
        }
    }
    ];

    datatable_config.fnRowCallback = function(nRow, aData, iDisplayIndex){
        var td = $("td.status", nRow);
        if(td.text() == 'Silent'){
            td.addClass("status-silent");
        } else if(td.text() == 'Reporting'){
            td.addClass("status-reporting");
        }       
        return nRow;
    };
    
    datatable_config.sAjaxSource = "JourneyTimeDatatable";
    datatable_config.sDom = "<\"H\"l<\"datatable-buttons\">r>t<\"F\"ip>";
    datatable_config.fnServerData = get_datatable_request_function({}, filter_div);

    return datatable_config;
}

