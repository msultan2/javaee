function init_logical_group(logical_group_name){
    setPageHeader(logical_group_name);
    create_logical_group_tables(logical_group_name);
}

function setPageHeader(logical_group_name){
    $("#logicalGroup").text(logical_group_name);
}

function create_logical_group_tables(logical_group_name){
    create_instation_user_logical_group_datatable(logical_group_name);
    create_route_logical_group_datatable(logical_group_name);
    create_span_logical_group_datatable(logical_group_name);
    create_detector_logical_group_datatable(logical_group_name);
}

function create_instation_user_logical_group_datatable(logical_group_name){
    var table = $("#instation-user-logical-group-table");
    var filter_div = $("<div/>");
    var table_headers = $("th", table);
    var instation_user_datatable_config = create_instation_user_logical_group_datatable_config(logical_group_name, filter_div);
    datatables['instation-user-logical-group-table'] = table.dataTable(instation_user_datatable_config);
    var datatable = datatables['instation-user-logical-group-table'];

    $(".datatable-buttons", table.closest(".dataTables_wrapper"))
    .append(create_show_hide_column_button({
        dialog:$("#instation-users-show-hide-column-dialog"),
        headers:table_headers,
        datatable:datatable,
        aoColumnDefs:instation_user_datatable_config.aoColumnDefs
    }))
    .append(create_filter_button({
        dialog:$("#instation-users-filter-dialog"),
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
        }
        ]
    }));
}

function create_route_logical_group_datatable(logical_group_name){   
    var table = $("#route-logical-group-table");
    var filter_div = $("<div/>");
    var table_headers = $("th", table);
    var route_logical_group_datatable_config = create_route_logical_group_datatable_config(logical_group_name, filter_div);
    datatables['route-logical-group-table'] = table.dataTable(route_logical_group_datatable_config);
    var datatable = datatables['route-logical-group-table'];

    $(".datatable-buttons", table.closest(".dataTables_wrapper"))
    .append(create_show_hide_column_button({
        dialog:$("#route-show-hide-column-dialog"),
        headers:table_headers,
        datatable:datatable,
        aoColumnDefs:route_logical_group_datatable_config.aoColumnDefs
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
        }
        ]
    }));
}

function create_span_logical_group_datatable(logical_group_name){
    var table = $("#span-logical-group-table");
    var filter_div = $("<div/>");
    var table_headers = $("th", table);
    var span_logical_group_datatable_config = create_span_logical_group_datatable_config(logical_group_name, filter_div);
    datatables['span-logical-group-table'] = table.dataTable(span_logical_group_datatable_config);
    var datatable = datatables['span-logical-group-table'];

    $(".datatable-buttons", table.closest(".dataTables_wrapper"))
    .append(create_show_hide_column_button({
        dialog:$("#span-show-hide-column-dialog"),
        headers:table_headers,
        datatable:datatable,
        aoColumnDefs:span_logical_group_datatable_config.aoColumnDefs
    }))
    .append(create_filter_button({
        dialog:$("#span-filter-dialog"),
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
        }
        ]
    }));
}

function create_detector_logical_group_datatable(logical_group_name){
    var table = $("#detector-logical-group-table");
    var filter_div = $("<div/>");
    var table_headers = $("th", table);
    var detector_logical_group_datatable_config = create_detector_logical_group_datatable_config(logical_group_name, filter_div);
    datatables['detector-logical-group-table'] = table.dataTable(detector_logical_group_datatable_config);
    var datatable = datatables['detector-logical-group-table'];

    $(".datatable-buttons", table.closest(".dataTables_wrapper"))
    .append(create_show_hide_column_button({
        dialog:$("#detector-show-hide-column-dialog"),
        headers:table_headers,
        datatable:datatable,
        aoColumnDefs:detector_logical_group_datatable_config.aoColumnDefs
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
        }
        ]
    }));
}

function create_instation_user_logical_group_datatable_config(logical_group_name, filter_div){
    var datatable_config = common_datatable_configuration();
    datatable_config.aoColumnDefs = [{
        "sClass":"text",
        "aTargets":[0,1]
    },{
        "sClass":"center",
        "aTargets":[2]
    },
    {
        "sWidth":"20px",
        "aTargets":[2]
    }];

    datatable_config.fnRowCallback = function(nRow, aData, iDisplayIndex){
        if (aData[2] == null){
            $(nRow).addClass("removed");
        } else {
            $(nRow).addClass("added");
        }
    }
    
    datatable_config.aoColumns = [
    null,
    null,
    {
        "mDataProp":function(source, type, val){
            if(source[2] == undefined){
                return "<a class='add-user add' title='Add user ["+source[0]+"] to ["+logical_group_name+"] group?' message='Are you sure you wish to add user ["+source[0]+"] to logical group?' username='"+source[1]+"' logicalgroupname='"+logical_group_name+"'>Add</a>";
            } else {
                return "<a class='remove-user remove' title='Remove user ["+source[0]+"] from ["+logical_group_name+"] group?' message='Are you sure you wish to remove user ["+source[0]+"] from logical group?' username='"+source[1]+"' logicalgroupname='"+logical_group_name+"'>Remove</a>";
            }
        }
    }
    ];

    datatable_config.fnServerParams = function(aoData){
        aoData.push({
            "name":"logical_group_name",
            "value":logical_group_name
        });
    };

    datatable_config.fnDrawCallback = function(){
        create_datatable_links(datatables['instation-user-logical-group-table'], {}, {});
    };

    datatable_config.sAjaxSource = "LogicalGroupInstationUserDatatable";
    datatable_config.sDom = "<\"H\"l<\"datatable-buttons\">r>t<\"F\"ip>";
    datatable_config.fnServerData = get_datatable_request_function({}, filter_div);

    return datatable_config;
}

function create_detector_logical_group_datatable_config(logical_group_name, filter_div){
    var datatable_config = common_datatable_configuration();
    datatable_config.sAjaxSource = "LogicalGroupDetectorDatatable";
    datatable_config.aoColumnDefs = [{
        "sClass":"text",
        "aTargets":[0,1,2]
    },{
        "sClass":"center",
        "aTargets":[3]
    },
    {
        "sWidth":"20px",
        "aTargets":[3]
    }];

    datatable_config.fnRowCallback = function(nRow, aData, iDisplayIndex){
        if (aData[3] == null){
            $(nRow).addClass("removed");
        } else {
            $(nRow).addClass("added");
        }
    }
    
    datatable_config.aoColumns = [
    null,
    null,
    null,
    {
        "mDataProp":function(source, type, val){
            if(source[3] == undefined){
                return "<a class='add-detector add' title='Add detector ["+source[0]+"] to ["+logical_group_name+"] group?' message='Are you sure you wish to add detector ["+source[0]+"] to logical group?' detector='"+source[1]+"' logicalgroupname='"+logical_group_name+"'>Add</a>";
            } else {
                return "<a class='remove-detector remove' title='Remove detector ["+source[0]+"] from ["+logical_group_name+"] group?' message='Are you sure you wish to remove detector ["+source[0]+"] from logical group?' detector='"+source[1]+"' logicalgroupname='"+logical_group_name+"'>Remove</a>";
            }
        }
    }
    ];

    datatable_config.fnServerParams = function(aoData){
        aoData.push({
            "name":"logical_group_name",
            "value":logical_group_name
        });
    };

    datatable_config.fnDrawCallback = function(){
        create_datatable_links(datatables['detector-logical-group-table'], {}, {});
    };

    datatable_config.sDom = "<\"H\"l<\"datatable-buttons\">r>t<\"F\"ip>";
    datatable_config.fnServerData = get_datatable_request_function({}, filter_div);

    return datatable_config;
}

function create_span_logical_group_datatable_config(logical_group_name, filter_div){
    var datatable_config = common_datatable_configuration();
    datatable_config.sAjaxSource = "LogicalGroupSpanDatatable";
    datatable_config.aoColumnDefs = [{
        "sClass":"text",
        "aTargets":[0]
    },{
        "sClass":"center",
        "aTargets":[1,2,3]
    },
    {
        "sWidth":"20px",
        "aTargets":[3]
    }];

    datatable_config.fnRowCallback = function(nRow, aData, iDisplayIndex){
        if (aData[3] == null){
            $(nRow).addClass("removed");
        } else {
            $(nRow).addClass("added");
        }
    }
    
    datatable_config.aoColumns = [
    null,
    null,
    null,
    {
        "mDataProp":function(source, type, val){
            if(source[3] == undefined){
                return "<a class='add-span add' title='Add span ["+source[1]+"] to ["+logical_group_name+"] group?' message='Are you sure you wish to add span ["+source[1]+"] to logical group?' span='"+source[0]+"' logicalgroupname='"+logical_group_name+"'>Add</a>";
            } else {
                return "<a class='remove-span remove' title='Remove span ["+source[1]+"] from ["+logical_group_name+"] group?' message='Are you sure you wish to remove span ["+source[1]+"] from logical group?' span='"+source[0]+"' logicalgroupname='"+logical_group_name+"'>Remove</a>";
            }
        }
    }
    ];

    datatable_config.fnServerParams = function(aoData){
        aoData.push({
            "name":"logical_group_name",
            "value":logical_group_name
        });
    };

    datatable_config.fnDrawCallback = function(){
        create_datatable_links(datatables['span-logical-group-table'], {}, {});
    };

    datatable_config.sDom = "<\"H\"l<\"datatable-buttons\">r>t<\"F\"ip>";
    datatable_config.fnServerData = get_datatable_request_function({}, filter_div);

    return datatable_config;
}

function create_route_logical_group_datatable_config(logical_group_name, filter_div){
    var datatable_config = common_datatable_configuration();
    datatable_config.sAjaxSource = "LogicalGroupRouteDatatable";
    datatable_config.aoColumnDefs = [{
        "sClass":"text",
        "aTargets":[0,1]
    },{
        "sClass":"center",
        "aTargets":[2]
    },
    {
        "sWidth":"20px",
        "aTargets":[2]
    }];

    datatable_config.fnRowCallback = function(nRow, aData, iDisplayIndex){        
        if (aData[2] == null){
            $(nRow).addClass("removed");
        } else {
            $(nRow).addClass("added");
        }
    }
    
    datatable_config.aoColumns = [
    null,
    null,
    {
        "mDataProp":function(source, type, val){
            if(source[2] == undefined){
                return "<a class='add-route add' title='Add route ["+source[0]+"] to ["+logical_group_name+"] group?' message='Are you sure you wish to add route ["+source[0]+"] to logical group?' route='"+source[0]+"' logicalgroupname='"+logical_group_name+"'>Add</a>";
            } else {
                return "<a class='remove-route remove' title='Remove route ["+source[0]+"] from ["+logical_group_name+"] group?' message='Are you sure you wish to remove route ["+source[0]+"] from logical group?' route='"+source[0]+"' logicalgroupname='"+logical_group_name+"'>Remove</a>";
            }
        }
    }
    ];

    datatable_config.fnServerParams = function(aoData){
        aoData.push({
            "name":"logical_group_name",
            "value":logical_group_name
        });
    };

    datatable_config.fnDrawCallback = function(){
        create_datatable_links(datatables['route-logical-group-table'], {}, {});
    };

    datatable_config.sDom = "<\"H\"l<\"datatable-buttons\">r>t<\"F\"ip>";
    datatable_config.fnServerData = get_datatable_request_function({}, filter_div);

    return datatable_config;
}

