// SCJS 009 

function init_diagnostic_spans(){
    create_span_table();
}

function create_span_table(){
    var logical_group_index = 8;
    var recordType = "Spans";
    var table = $("#span-table");
    var filter_div = $("<div/>");
    var table_headers = $("th", table);
    var span_datatable_config = create_span_datatable_config(filter_div);
    datatables['span-table'] = table.dataTable(span_datatable_config);
    var datatable = datatables['span-table'];

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
            ajax_url:span_datatable_config.sAjaxSource
        }))
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
            datatable_headers:table_headers,
            logical_group_index:logical_group_index,
            recordType: recordType,
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
            },
            {
                "target":9,
                "type":"string"
            }
            ]
        }));

    },
    "json");
}

function create_span_datatable_config(filter_div){
    var datatable_config = common_datatable_configuration();
    datatable_config.aoColumnDefs = [
    {
        "sClass":"text",
        "aTargets":[0]
    },
    {
        "sClass":"detector",
        "aTargets":[1,2]
    },
    {
        "sClass":"center",
        "aTargets":[3,4,5,6,7]
    },
    {
        "aTargets": [3,4,5,6,7,8],
        "bVisible": false
    },
    {
        "aTargets": [9],
        "sClass": 'status center'
    },
    {
        "sClass": "numeric",
        "aTargets": [3]
    }
    ];

    datatable_config.aoColumns = [
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null
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

    datatable_config.sAjaxSource = "DiagnosticSpanDatatable";
    datatable_config.sDom = "<\"H\"l<\"datatable-buttons\">r>t<\"F\"ip>";
    datatable_config.fnServerData = get_datatable_request_function({}, filter_div);

    return datatable_config;
}