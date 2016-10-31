function init_fault_reports() {
    create_fault_datatable();


}

function create_fault_datatable() {

    var logical_group_index = 3;
    var recordType = "Fault Reports";
    var table = $("#fault-table");
    var filter_div = $("<div/>");
    var table_headers = $("th", table);
    var fault_datatable_config = create_fault_datatable_config(filter_div);
    datatables['fault-table'] = table.dataTable(fault_datatable_config);
    var datatable = datatables['fault-table'];

    var logical_group_options = [];
    $.post("CurrentUserManager",
            {
                action: "user"
            },
    function(data) {
        logical_group_options = [];
        for (var i = 0; i < data.data.logical_groups.length; i++) {
            logical_group_options.push("[" + data.data.logical_groups[i] + "]");
        }
        // Quick filter
        create_quick_filter(table, logical_group_options, logical_group_index, recordType, table_headers);
        $(".cannot-edit").remove();
        $(".datatable-buttons", table.closest(".dataTables_wrapper"))
                .append(create_csv_download_button({
            filter_div: filter_div,
            datatable: datatable,
            ajax_url: fault_datatable_config.sAjaxSource
        }))
                .append(create_show_hide_column_button({
            dialog: $("#fault-show-hide-column-dialog"),
            headers: table_headers,
            datatable: datatable,
            aoColumnDefs: fault_datatable_config.aoColumnDefs
        }))
                .append(create_filter_button({
            dialog: $("#fault-filter-dialog"),
            filter_div: filter_div,
            datatable: datatable,
            logical_group_index: logical_group_index,
            recordType: recordType,
            datatable_headers: table_headers,
            column_filter_definitions: [{
                    "target": 0,
                    "type": "string"
                },
                {
                    "target": 1,
                    "type": "string"
                },
                {
                    "target": 2,
                    "type": "time"
                },
                {
                    "target": logical_group_index,
                    "type": "enum",
                    "options": logical_group_options
                }
            ]
        }));

    },
            "json");

    start_datatable_refresh_interval();
}

function create_fault_datatable_config(filter_div) {
    var datatable_config = common_datatable_configuration();
    datatable_config.aoColumnDefs = [
        {
            "sClass": "center",
            "aTargets": [1, 2, 3]
        },
        {
            "bVisible": false,
            "aTargets": [0]
        },
        {
            "sWidth": "20px",
            "aTargets": [3]
        }
    ];

    datatable_config.aoColumns = [
        null,
        null,
        {
            "mDataProp": function(source, type, val) {
                return source[2].substring(0, 19);
            }
        },
        {
            "mDataProp": function(source, type, val) {
                return $("<a>")
                        .addClass("view-fault")
                        .attr("href", "/Fault?reportId=" + source[0])
                        .text("Faults")[0].outerHTML;
            }
        }
    ];

    datatable_config.aaSorting = [[2, "desc"]];

    datatable_config.iDisplayLength = 25;

    datatable_config.fnDrawCallback = function() {
        $('a.view-fault', datatables['fault-table'])
                .button({
            icons: {
                primary: "ui-icon-search"
            }
        })
                .addClass("ui-state-highlight")
                .bind({
            click: function() {

            }
        });
    };


    datatable_config.sAjaxSource = "FaultReportDatatable";
    datatable_config.sDom = "<\"H\"l<\"datatable-buttons\">r>t<\"F\"ip>";
    datatable_config.fnServerData = get_datatable_request_function({}, filter_div);

    return datatable_config;
}

