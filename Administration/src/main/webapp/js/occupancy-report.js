function init_occupancy_reports(){
    create_occupancy_datatable();

    $("#graphs").hide();
    $("img", "#graphs").hide().load(function(){
        var img = $(this);
        var img_id = img.attr("id");
        $("#p-"+img_id+"-loading").hide();
        img.show();
    });
}

function create_occupancy_datatable(){

    var logical_group_index = 8;
    var recordType = "Occupancy Reports";
    var table = $("#occupancy-table");
    var filter_div = $("<div/>");
    var table_headers = $("th", table);
    var occupancy_datatable_config = create_occupancy_datatable_config(filter_div);
    datatables['occupancy-table'] = table.dataTable(occupancy_datatable_config);
    var datatable = datatables['occupancy-table'];

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
            ajax_url:occupancy_datatable_config.sAjaxSource
        }))
        .append(create_show_hide_column_button({
            dialog:$("#occupancy-show-hide-column-dialog"),
            headers:table_headers,
            datatable:datatable,
            aoColumnDefs:occupancy_datatable_config.aoColumnDefs
        }))
        .append(create_filter_button({
            dialog:$("#occupancy-filter-dialog"),
            filter_div:filter_div,
            datatable:datatable,
            logical_group_index:logical_group_index,
            recordType: recordType,
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
                "type":"time"
            },
            {
                "target":3,
                "type":"integer"
            },
            {
                "target":4,
                "type":"integer"
            },
            {
                "target":5,
                "type":"integer"
            },
            {
                "target":6,
                "type":"integer"
            },
            {
                "target":7,
                "type":"integer"
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

function create_occupancy_datatable_config(filter_div){
    var datatable_config = common_datatable_configuration();
    datatable_config.aoColumnDefs =[
    {
        "sClass":"center",
        "aTargets":[2,8,10]
    },

    {
        "sClass":"occupancy-stationary center",
        "aTargets":[3]
    },

    {
        "sClass":"occupancy-very-slow center",
        "aTargets":[4]
    },

    {
        "sClass":"occupancy-slow center",
        "aTargets":[5]
    },

    {
        "sClass":"occupancy-moderate center",
        "aTargets":[6]
    },
    {
        "sClass":"occupancy-free center",
        "aTargets":[7]
    },    
    {
        "bVisible": false,
        "aTargets":[0,9]
    },
    {
        "sWidth":"20px",
        "aTargets":[10]
    }
    ];
    
    datatable_config.aoColumns = [
    null,
    null,
    {
        "mDataProp":function(source, type, val){
            return source[2].substring(0, 19);
        }
    },
    null,
    null,
    null,
    null,
    null,
    null,
    {
        "mDataProp":function(source, type, val){
            return source[8].replace(/\] \[/g, "] <br/> [");
        }
    },
    {
        "mDataProp":function(source, type, val){
            return "<a class='view-graph' title='Select ["+source[1]+"]' detectorname='"+source[1]+"' detectorid='"+source[0]+"'>select</a>";
        }
    }
    ];
    
    datatable_config.iDisplayLength = 25;

    datatable_config.fnDrawCallback = function(){
        $('a.view-graph', datatables['occupancy-table'])
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


    datatable_config.sAjaxSource = "OccupancyDatatable";
    datatable_config.sDom = "<\"H\"l<\"datatable-buttons\">r>t<\"F\"ip>";
    datatable_config.fnServerData = get_datatable_request_function({}, filter_div);

    return datatable_config;
}

function display_detector_graph_information(detector_name, detector_id) {
    var graphs = $("#graphs");
    var img = $("img", "#graphs");
    img.hide();
    $("p", "#graphs").show();
    graphs.show();
    $("h1", "#graphs").text("Traffic Flow Graphs - "+detector_name+":"+detector_id);

    $("#a-occupancy-last-hour").text("Traffic flow reports in the last hour");
    $("#h2-occupancy-last-hour").text(detector_name+":"+detector_id+" - Traffic flow  reports in the last hour");
    $("#occupancy-last-hour").attr("src","Occupancy/OccupancyLastHourChart?detectorName="+detector_name+"&detectorId="+detector_id+"&w="+img.width()+"&h=300&rndm="+Math.random());

    $("#a-occupancy-last-day").text("Traffic flow reports in the last day");
    $("#h2-occupancy-last-day").text(detector_name+":"+detector_id+" - Traffic flow  reports in the last day");
    $("#occupancy-last-day").attr("src","Occupancy/OccupancyLastDayChart?detectorName="+detector_name+"&detectorId="+detector_id+"&w="+img.width()+"&h=300&rndm="+Math.random());
}

