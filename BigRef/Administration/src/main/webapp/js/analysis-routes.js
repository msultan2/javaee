function init_analysis(){    
    create_analysis_route_table();    
}

function create_analysis_route_table(){
    var logical_group_index = 2;
    var recordType = "Routes";
    var table = $("#routes-table");
    var filter_div = $("<div/>");
    var table_headers = $("th", table);
    var routes_datatable_config = create_routes_datatable_config(filter_div);
    datatables['routes-table'] = table.dataTable(routes_datatable_config);
    var datatable = datatables['routes-table'];

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
            ajax_url:routes_datatable_config.sAjaxSource
        }))
        .append(create_show_hide_column_button({
            dialog:$("#route-show-hide-column-dialog"),
            headers:table_headers,
            datatable:datatable,
            aoColumnDefs:routes_datatable_config.aoColumnDefs
        }))
        .append(create_filter_button({
            dialog:$("#route-filter-dialog"),
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
                "target":logical_group_index,
                "type":"enum",
                "options": logical_group_options
            }
            ]
        }));
    },
    "json");
}

function create_routes_datatable_config(filter_div){
    var datatable_config = common_datatable_configuration();
    datatable_config.aoColumnDefs = [   
    
    {
        "sClass":"center",
        "aTargets":[3,4]
    },
    {
        "bSortable":false,
        "aTargets":[3,4]
    },
    {
        "sWidth":"120px",
        "aTargets":[3,4]
    },
    {
        "aTargets": [2],
        "bVisible": false
    }
    ];

    datatable_config.aoColumns = [
    null,
    null,
    {
        "mDataProp":function(source, type, val){
            return source[2].replace(/\] \[/g, "] <br/> [");
        }
    },
    {
        "mDataProp":function(source, type, val){
            return "<a class='view-graph' href='AnalysisRouteDuration?route="+source[0]+"' title='View ["+source[0]+"] Duration Data' >View Duration</a>";
        }
    },
    {
        "mDataProp":function(source, type, val){
            return "<a class='view-graph' href='AnalysisRouteSpeed?route="+source[0]+"' title='View ["+source[0]+"] Speed Data' >View Speed</a>";
        }
    }
    ];

    datatable_config.fnDrawCallback = function(){
        $('a.view-graph', datatables['route-table'])
        .button({
            icons:{
                primary:"ui-icon-search"
            }
        })
        .addClass("ui-state-highlight")        
    };

    datatable_config.sAjaxSource = "AnalysisRoutesDatatable";
    datatable_config.sDom = "<\"H\"l<\"datatable-buttons\">r>t<\"F\"ip>";
    datatable_config.fnServerData = get_datatable_request_function({}, filter_div);

    return datatable_config;
}