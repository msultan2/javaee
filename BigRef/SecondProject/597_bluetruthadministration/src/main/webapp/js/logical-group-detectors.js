// ADDED IN SCJS 009

function init_logical_group(logical_group_name){
    setHeader(logical_group_name);
    create_detector_logical_group_datatable(logical_group_name);
}

function setHeader(logical_group_name){
   $("#logical_group_name > a").text(logical_group_name).attr("href","/LogicalGroup?group="+logical_group_name);
}

function create_detector_logical_group_datatable(logical_group_name){
    var table = $("#detector-logical-group-table");
    var filter_div = $("<div/>");
    var table_headers = $("th", table);
    var detector_logical_group_datatable_config = create_detector_logical_group_datatable_config(logical_group_name, filter_div);
    datatables['detector-logical-group-table'] = table.dataTable(detector_logical_group_datatable_config);
    var datatable = datatables['detector-logical-group-table'];

    $(".datatable-buttons", table.closest(".dataTables_wrapper"))
    .append(create_csv_download_button({
        filter_div:filter_div,
        datatable:datatable,
        parameters : [{"name":"logical_group_name", "value":logical_group_name}],
        ajax_url:detector_logical_group_datatable_config.sAjaxSource
    }))
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
