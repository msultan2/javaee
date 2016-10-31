function init_detector_status() {
    create_status_datatable();
}

function create_status_datatable() {

    var logical_group_index = -1;
    var recordType = "Detector Status";
    var table = $("#status-table");
    var filter_div = $("<div/>");
    var table_headers = $("th", table);
    var fault_datatable_config = create_status_datatable_config(filter_div);
    datatables['status-table'] = table.dataTable(fault_datatable_config);
    var datatable = datatables['status-table'];

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
            dialog: $("#status-show-hide-column-dialog"),
            headers: table_headers,
            datatable: datatable,
            aoColumnDefs: fault_datatable_config.aoColumnDefs
        }))
                .append(create_filter_button({
            dialog: $("#status-filter-dialog"),
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
                    "type": "string"
                },
                {
                    "target": 3,
                    "type": "string"
                },
                {
                    "target": 4,
                    "type": "string"
                },
                {
                    "target": 5,
                    "type": "integer"
                },
                {
                    "target": 6,
                    "type": "integer"
                },
                {
                    "target": 7,
                    "type": "integer"
                },
                {
                    "target": 8,
                    "type": "integer"
                },
                {
                    "target": 9,
                    "type": "integer"
                },
                {
                    "target": 10,
                    "type": "integer"
                },
                {
                    "target": 11,
                    "type": "integer"
                },
                {
                    "target": 12,
                    "type": "string"
                },
                {
                    "target": 13,
                    "type": "integer"
                },
                {
                    "target": 14,
                    "type": "string"
                },
                {
                    "target": 15,
                    "type": "string"
                },
                {
                    "target": 16,
                    "type": "time"
                }
            ]
        }));

    },
            "json");

    start_datatable_refresh_interval();
}

function create_status_datatable_config(filter_div) {
    var datatable_config = common_datatable_configuration();
    datatable_config.aoColumnDefs = [
        {
            "sClass": "center",
            "aTargets": [0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17]
        },
        {
            "sWidth":"150px",
            "aTargets":[17]
        },
        {
            "aTargets":[5,7,8,10,17],
            "bVisible": false
        }
    ];

    datatable_config.aoColumns = [
        null, //0
        null, //1
        null, //2
        null, //3
        null, //4
        {"mDataProp": function(source, type, val) { //5
                return source[5] == "-255"?"N/A" : source[5];
            }
        },
        {"mDataProp": function(source, type, val) { //6
                return source[5] == "-255"?"N/A" : source[6];
            }
        },
        {"mDataProp": function(source, type, val) { //7
                return source[5] == "-255"?"N/A" : source[7];
            }
        },
        {"mDataProp": function(source, type, val) { //8
                return source[5] == "-255"?"N/A" : source[8];
            }
        },
        {"mDataProp": function(source, type, val) { //9
                return source[5] == "-255"?"N/A" : source[9];
            }
        },
        {"mDataProp": function(source, type, val) { //10
                return source[5] == "-255"?"N/A" : source[10];
            }
        },
        {"mDataProp": function(source, type, val) { //11 Performance Index
                return source[5] == "-255"?"N/A" : source[11];
            }
        },
        {"mDataProp": function(source, type, val) { //12
                var ofnames = {"0": "None", "1": "RAND1"};
                return source[12] in ofnames ? ofnames[source[12]] : "?";
            }
        }, 
        null, //13
        null, //14
        null, //15
        {
            "mDataProp": function(source, type, val) { //16
                return source[16].substring(0, 19);
            }
        },
        {
        "mDataProp":function(source, type, val){ //17    
            return "<a class='view-graph' href='DetectorTwoGStatistics?detector_id="+source[0]+"&detector_name="+source[1]+"' title='View ["+source[0]+"] Detector 2G Statistics' >2G</a>\n\
                    <a class='view-graph' href='DetectorThreeGStatistics?detector_id="+source[0]+"&detector_name="+source[1]+"' title='View ["+source[0]+"] Detector 3G Statistics' >3G</a>\n\
                    <a class='view-graph' href='DetectorPerformanceIndex?detector_id="+source[0]+"&detector_name="+source[1]+"' title='View ["+source[0]+"] Detector Performance Index' >PI</a>";
        }
    }
    ];

    datatable_config.aaSorting = [[2, "desc"]];

    datatable_config.iDisplayLength = 25;

    datatable_config.fnDrawCallback = function() {
        $('a.view-graph', datatables['status-table'])
                .button()
                .addClass("ui-state-highlight")
                .bind({
            click: function() {
            }
        });
    };


    datatable_config.sAjaxSource = "StatusDatatable";
    datatable_config.sDom = "<\"H\"l<\"datatable-buttons\">r>t<\"F\"ip>";
    datatable_config.fnServerData = get_datatable_request_function({}, filter_div);

    return datatable_config;
}

