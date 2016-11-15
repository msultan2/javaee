function init_fault_messages(reportId) {
    create_fault_messages_datatable(reportId);
}

function create_fault_messages_datatable(reportId) {

    var logical_group_index = 1;
    var recordType = "Fault Message Reports";
    var table = $("#fault-messages-table");
    var filter_div = $("<div/>");
    var table_headers = $("th", table);
    var fault_messages_datatable_config = create_fault_messages_datatable_config(filter_div, reportId);
    datatables['fault-messages-table'] = table.dataTable(fault_messages_datatable_config);
    var datatable = datatables['fault-messages-table'];
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
        $(".datatable-buttons", table.closest(".dataTables_wrapper"))
                .append(create_csv_download_button({
            filter_div: filter_div,
            datatable: datatable,
            parameters : [{"name":"reportId", "value":reportId}],
            ajax_url: fault_messages_datatable_config.sAjaxSource
        }))
                .append(create_show_hide_column_button({
            dialog: $("#fault-messages-show-hide-column-dialog"),
            headers: table_headers,
            datatable: datatable,
            aoColumnDefs: fault_messages_datatable_config.aoColumnDefs
        }))
                .append(create_filter_button({
            dialog: $("#fault-messages-filter-dialog"),
            filter_div: filter_div,
            datatable: datatable,
            logical_group_index: 1,
            recordType: recordType,
            datatable_headers: table_headers,
            column_filter_definitions: [{
                    "target": 0,
                    "type": "integer"
                },
                {
                    "target": 2,
                    "type": "integer"
                },
                {
                    "target": 3,
                    "type": "time"
                }
            ]
        }));
    },
            "json");
}

function create_fault_messages_datatable_config(filter_div, reportId) {
    var datatable_config = common_datatable_configuration();
    datatable_config.aoColumnDefs = [
        {
            "sClass": "center",
            "aTargets": [0, 1, 2, 3]
        }
    ];
    datatable_config.aoColumns = [
        null,
        {
            "mDataProp": function(source, type, val) {

                return faultDescription(source[0]);
            }
        },
        {"mDataProp": function(source, type, val) {
                return source[1] == "0" ? "Cleared" : "Set";
            }
        },
        {
            "mDataProp": function(source, type, val) {
                return source[2].substring(0, 19);
            }
        }
    ];
    datatable_config.iDisplayLength = 25;
    datatable_config.aaSorting = [[2, "desc"]];
    datatable_config.fnDrawCallback = function() {
        $('a.view-fault-messages', datatables['fault-messages-table'])
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
    datatable_config.sAjaxSource = "FaultMessageDatatable";
    datatable_config.sDom = "<\"H\"l<\"datatable-buttons\">r>t<\"F\"ip>";
    datatable_config.fnServerData = get_datatable_request_function({
        reportId: reportId
    }, filter_div);
    return datatable_config;
}


function faultDescription(code) {
    var codes = {
        "51": "Functional configuration syntax error",
        "52": "Functional configuration invalid parameter/-s value",
        "60": "Missing or invalid seed file",
        "70": "Core ID not defined",
        "71": "Missing production data",
        "75": "Congestion report URI address not defined",
        "76": "Statistics report URI address not defined",
        "77": "Fault report URI address not defined",
        "78": "Status report URI address not defined",
        "100": "General device failure, device has been removed, device stopped communicating",
        "2\d1": "Retrieve Configuration Client",
        "2\d2": "Congestion Report Client",
        "2\d4": "Alert and Status Report Client",
        "2\d5": "Status Reporting Client",
        "2\d6": "Fault Reporting Client",
        "2\d7": "Statistics Reporting Client",
        "20\d": "Failure to establish communication to the InStation or InStation does not respond",
        "21\d": "InStation does not accept reports responding not with Status OK",
        "22\d": "InStation response message body contains error and the OutStation is not able tot parse it",
        "400": "GSM modem general fault",
        "500": "GPS module general fault",
        "501": "GPS module is unable to lock on satellites",
        "600": "RTCC does not tick",
        "700": "Mains Voltage below threshold"
    };
    return _.chain(codes).pairs().filter(function(p) {
        return (code + "").match(new RegExp("^"+p[0].replace("d","\\d")+"$","g"));
    }).map(function(p) {
        return p[1];
    }).value().join("/") || "Unknown fault";
}
