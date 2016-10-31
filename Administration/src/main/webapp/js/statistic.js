function init_statistic(reportId) {
    create_statistic_datatable(reportId);
}

function create_statistic_datatable(reportId) {

    var logical_group_index = 4;
    var recordType = "Statistics Reports";
    var table = $("#statistic-table");
    var filter_div = $("<div/>");
    var table_headers = $("th", table);
    var statistic_datatable_config = create_statistic_datatable_config(filter_div, reportId);
    datatables['statistic-table'] = table.dataTable(statistic_datatable_config);
    var datatable = datatables['statistic-table'];

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
            ajax_url: statistic_datatable_config.sAjaxSource
        }))
                .append(create_show_hide_column_button({
            dialog: $("#statistic-show-hide-column-dialog"),
            headers: table_headers,
            datatable: datatable,
            aoColumnDefs: statistic_datatable_config.aoColumnDefs
        }))
                .append(create_filter_button({
            dialog: $("#statistic-filter-dialog"),
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
                    "type": "integer"
                },
                {
                    "target": 2,
                    "type": "time"
                },
                {
                    "target": 3,
                    "type": "time"
                },
                {
                    "target": 4,
                    "type": "integer"
                }
            ]
        }));

    },
            "json");
}

function create_statistic_datatable_config(filter_div, reportId) {
    var datatable_config = common_datatable_configuration();
    datatable_config.aoColumnDefs = [
        {
            "sClass": "center",
            "aTargets": [1, 2, 3]
        },
         {
            "bVisible": false,
            "aTargets": [4]
        }
    ];

    datatable_config.aoColumns = [
        {
            "mDataProp": function(source, type, val) {
                
                // Address greater then 13 then no ":"/"0" added.
                var addressLength = 13;
                
                // 2 byte groups separated by character ':'
                function t(s) {
                    
                    if(s.length > addressLength){
                        return s;
                    }
                    
                    return s.substring(0, 2) + (s.length > 2 ? ":" + t(s.substring(2)) : "");
                }
                function ap(s) {
                    
                    if(s.length > addressLength){
                        return s;
                    }
                    
                    return new Array(13-s.length).join("0")+s;
                }
                
                // 0 - no hashing
                return source[4]==0? t(ap(source[0])) : source[0];
            }
        },
        {"mDataProp": function(source, type, val) {
                return $("<span>").text(codDecode(source[1]).dclass).attr("title", codDecode(source[1]).dservices.join(","))[0].outerHTML;
            }
        },
        {
            "mDataProp": function(source, type, val) {
                return source[2].substring(0, 19);
            }
        },
        {
            "mDataProp": function(source, type, val) {
                return (source[3] || "").substring(0, 19);
            }
        },
        null
    ];

    datatable_config.iDisplayLength = 25;
    datatable_config.aaSorting = [[2, "desc"]];
    datatable_config.fnDrawCallback = function() {
        $('a.view-statistic', datatables['statistic-table'])
                .button({
            icons: {
                primary: "ui-icon-search"
            }
        })
                .addClass("ui-state-highlight")
                .bind({
            click: function() {
//                var detector_name = $(this).attr("detectorname");
//                var detector_id = $(this).attr("detectorid");
//                alert(detector_name);
            }
        });
    };


    datatable_config.sAjaxSource = "StatisticDatatable";
    datatable_config.sDom = "<\"H\"l<\"datatable-buttons\">r>t<\"F\"ip>";
    datatable_config.fnServerData = get_datatable_request_function({
        reportId: reportId
    }, filter_div);
    return datatable_config;
}

