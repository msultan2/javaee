function init_route_spans(route_name){
    setRouteName(route_name);
    create_route_spans_table(route_name);
}

function setRouteName(route_name){
    $("#routeName").text(route_name);
}

function create_route_spans_table(route_name){
    var logical_group_index = 1;
    var recordType = "Spans";
    var table = $("#route-spans-table");
    var filter_div = $("<div/>");
    var table_headers = $("th", table);
    var span_datatable_config = create_routes_datatable_config(route_name, filter_div);
    datatables['route-spans-table'] = table.dataTable(span_datatable_config);
    var datatable = datatables['route-spans-table'];

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
        .append(create_show_hide_column_button({
            dialog:$("#span-show-hide-column-dialog"),
            headers:table_headers,
            datatable:datatable,
            aoColumnDefs:span_datatable_config.aoColumnDefs
        }))
        .append(create_filter_button({
            dialog:$("#span-filter-dialog"),
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
                "target":logical_group_index,
                "type":"enum",
                "options": logical_group_options
            }
            ]
        }));
    },
    "json");
}

function create_routes_datatable_config(route_name, filter_div){
    var datatable_config = common_datatable_configuration();
    datatable_config.sAjaxSource = "RouteSpanDatatable";
    datatable_config.aoColumnDefs = [{
        "sClass":"text",
        "aTargets":[0]
    },{
        "sClass":"center",
        "aTargets":[1]
    },
    {
        "sWidth":"20px",
        "aTargets":[2]
    }];

    datatable_config.aoColumns = [
    null,
    null,
    {
        "mDataProp":function(source, type, val){
            if(source[2] == undefined){
                return "<a class='add-route-span add' title='Add span ["+source[0]+"] to ["+route_name+"] group?' message='Are you sure you wish to add span ["+source[1]+"] to logical group?' span='"+source[0]+"' routename='"+route_name+"'>Add</a>";
            } else {
                return "<a class='remove-route-span remove' title='Remove span ["+source[0]+"] from ["+route_name+"] group?' message='Are you sure you wish to remove span ["+source[1]+"] from logical group?' span='"+source[0]+"' routename='"+route_name+"'>Remove</a>";
            }
        }
    }
    ];

    datatable_config.fnRowCallback = function(nRow, aData, iDisplayIndex){
        if (aData[2] == null){
            $(nRow).addClass("removed");
        } else {
            $(nRow).addClass("added");
        }
    }
    datatable_config.aaSorting = [[1, "asc"]];

    datatable_config.fnServerParams = function(aoData){
        aoData.push({
            "name":"route",
            "value":route_name
        });
    };

    datatable_config.fnDrawCallback = function(){
        create_datatable_links(datatables['route-spans-table'], {}, {});
    };
    
    datatable_config.sDom = "<\"H\"l<\"datatable-buttons\">r>t<\"F\"ip>";
    datatable_config.fnServerData = get_datatable_request_function({}, filter_div);

    return datatable_config;
}
