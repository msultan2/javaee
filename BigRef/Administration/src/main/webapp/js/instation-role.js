function init_instation_role(){
    $("#instation-role-link").attr("href","#");
    create_instation_role_table();
}

function create_instation_role_table(){   
    var table = $("#instation-role-table");
    var filter_div = $("<div/>");
    var table_headers = $("th", table);
    var instation_role_datatable_config = create_instation_role_datatable_config(filter_div);
    datatables['instation-role-table'] = table.dataTable(instation_role_datatable_config);
    var datatable = datatables['instation-role-table'];

    $(".datatable-buttons", table.closest(".dataTables_wrapper"))
        .append(create_csv_download_button({
            filter_div:filter_div,
            datatable:datatable,
            ajax_url:instation_role_datatable_config.sAjaxSource
        }))
        .append(create_show_hide_column_button({
            dialog:$("#instation-user-roles-show-hide-column-dialog"),
            headers:table_headers,
            datatable:datatable,
            aoColumnDefs:instation_role_datatable_config.aoColumnDefs
        }))
        .append(create_filter_button({
            dialog:$("#instation-user-roles-filter-dialog"),
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

function create_instation_role_datatable_config(filter_div){
    var datatable_config = common_datatable_configuration();
    datatable_config.sAjaxSource = "InstationRoleDatatable";
    datatable_config.aoColumnDefs = [{
        "sClass":"text",
        "aTargets":[0,1]
    }];

    datatable_config.aoColumns = [
    null,
    null    
    ];

    datatable_config.sDom = "<\"H\"l<\"datatable-buttons\">r>t<\"F\"ip>";
    datatable_config.fnServerData = get_datatable_request_function({}, filter_div);

    return datatable_config;
}



