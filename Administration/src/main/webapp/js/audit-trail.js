/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


function init_audit_trail(){
    create_audit_trail_table();
}

function create_audit_trail_table(){
    var table = $("#audit-trail-table");
    var filter_div = $("<div/>");
    var table_headers = $("th", table);
    var audit_trail_datatable_config = create_audit_trail_datatable_config(filter_div, table);
    datatables['audit-trail-table'] = table.dataTable(audit_trail_datatable_config);
    var datatable = datatables['audit-trail-table'];
    
    $.post("AuditTrailActionManager",
    {
        action:"get"
    },
    function(data){
        user_action_options = [];
        for(var i = 0; i <data.data.length; i++){
            user_action_options.push("["+data.data[i]+"]");
        }
        $(".datatable-buttons", table.closest(".dataTables_wrapper"))
        .append(create_show_hide_column_button({
            dialog:$("#audit-trail-show-hide-column-dialog"),
            headers:table_headers,
            datatable:datatable,
            aoColumnDefs:audit_trail_datatable_config.aoColumnDefs
        }))
        .append(create_filter_button({
            dialog:$("#audit-trail-filter-dialog"),
            filter_div:filter_div,
            datatable:datatable,
            datatable_headers:table_headers,
            column_filter_definitions:[{
                "bVisible": false,
                "aTargets":[0]
            },
            {
                "target":1,
                "type":"string"
            },
            {
                "target":2,
                "type":"date"
            }
            ,
            {
                "target":3,
                "type":"enum",
                "options": user_action_options
            },
            {
                "target":4,
                "type":"string"
            }
            ]
        }));
    },
    "json");
}

function create_audit_trail_datatable_config(filter_div, table){
    var datatable_config = common_datatable_configuration();
    datatable_config.aoColumnDefs = [
    {
        "sClass":"center",
        "aTargets":[0,1,2,3]
    },
    {
        "aTargets": [0],
        "bVisible": false
    }
    ];

    datatable_config.aoColumns = [
    null,
    null,
    null,
    null,
    null
    ];

    datatable_config.aaSorting=[[2,"desc"]],
    
    datatable_config.sAjaxSource = "AuditTrailDatatable";
    datatable_config.sDom = "<\"H\"l<\"datatable-buttons\">r>t<\"F\"ip>";
    datatable_config.fnServerData = get_datatable_request_function(
    {
    }, filter_div);

    return datatable_config;
}