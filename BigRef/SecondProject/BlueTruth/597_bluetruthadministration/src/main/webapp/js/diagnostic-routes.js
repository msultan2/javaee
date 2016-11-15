
// SCJS 014 START

function init_routes(){
    $("#routes-link").attr("href","#");
    create_routes_table();
}

function create_routes_table(){
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
        create_quick_filter(table, logical_group_options, logical_group_index, recordType, table_headers);
        $(".datatable-buttons", table.closest(".dataTables_wrapper"))
        .append(create_csv_download_button({
            filter_div:filter_div,
            datatable:datatable,
            ajax_url: routes_datatable_config.sAjaxSource
        }))
        .append(create_show_hide_column_button({
            dialog:$("#diagnostic-route-show-hide-column-dialog"),
            headers:table_headers,
            datatable:datatable,
            aoColumnDefs:routes_datatable_config.aoColumnDefs
        }))
        .append(create_filter_button({
            dialog:$("#diagnostic-route-filter-dialog"),
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
                "target":logical_group_index,
                "type":"enum",
                "options": logical_group_options
            },
            {
                "target":3,
                "type":"string"
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
        "aTargets": [3],
        "sClass": 'status center'
    },    
    {
        "sWidth":"80px",
        "aTargets":[3]
    },
    {
        "aTargets": [2],
        "bVisible": false
    }];

    datatable_config.aoColumns = [
    null,
    null,
    {
        "mDataProp":function(source, type, val){
            return source[2].replace(/\] \[/g, "] <br/> [");
        }
    },
    null
    ];

    datatable_config.fnDrawCallback = function(){
        create_datatable_links(datatables['routes-table'], {}, {});
    };

    datatable_config.fnRowCallback = function(nRow, aData, iDisplayIndex){
        var td = $("td.status", nRow);
        if(td.text() == 'Silent'){
            td.addClass("status-silent");
        } else if(td.text() == 'Reporting'){
            td.addClass("status-reporting");
        }
        return nRow;
    };

    datatable_config.sAjaxSource = "DiagnosticRouteDatatable";
    datatable_config.sDom = "<\"H\"l<\"datatable-buttons\">r>t<\"F\"ip>";
    datatable_config.fnServerData = get_datatable_request_function({}, filter_div);

    return datatable_config;
}

// SCJS 014 END

