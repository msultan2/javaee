function init_statistics_reports() {
    create_statistics_datatable();

    $("#graphs").hide();
    $("img", "#graphs").hide().load(function() {
        var img = $(this);
        var img_id = img.attr("id");
        $("#p-" + img_id + "-loading").hide();
        img.show();
    });
}

function create_statistics_datatable() {

    var logical_group_index = 4;
    var recordType = "Statistics Reports";
    var table = $("#statistics-table");
    var filter_div = $("<div/>");
    var table_headers = $("th", table);
    var statistics_datatable_config = create_statistics_datatable_config(filter_div);
    datatables['statistics-table'] = table.dataTable(statistics_datatable_config);
    var datatable = datatables['statistics-table'];

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
            ajax_url: statistics_datatable_config.sAjaxSource
        }))
                .append(create_show_hide_column_button({
            dialog: $("#statistics-show-hide-column-dialog"),
            headers: table_headers,
            datatable: datatable,
            aoColumnDefs: statistics_datatable_config.aoColumnDefs
        }))
                .append(create_filter_button({
            dialog: $("#statistics-filter-dialog"),
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
                    "target": 3,
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

function create_statistics_datatable_config(filter_div) {
    var datatable_config = common_datatable_configuration();
    datatable_config.aoColumnDefs = [
        {
            "sClass": "center",
            "aTargets": [1, 2, 3, 4]
        },
        {
            "bVisible": false,
            "aTargets": [0]
        },
        {
            "sWidth": "20px",
            "aTargets": [4]
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
                return source[3].substring(0, 19);
            }
        },
        {
            "mDataProp": function(source, type, val) {
                return $("<a>")
                        .addClass("view-statistics")
                        .attr("href", "/Statistic?reportId=" + source[0])
                        .text("Devices")[0].outerHTML;
            }
        }
    ];

    datatable_config.aaSorting = [[2, "desc"]];

    datatable_config.iDisplayLength = 25;

    datatable_config.fnDrawCallback = function() {
        $('a.view-statistics', datatables['statistics-table'])
                .button({
            icons: {
                primary: "ui-icon-search"
            }
        })
                .addClass("ui-state-highlight")
                .bind({
            click: function() {
                var reportId = $(this).attr("reportId");
                alert(detector_name);
            }
        });
    };


    datatable_config.sAjaxSource = "StatisticsDatatable";
    datatable_config.sDom = "<\"H\"l<\"datatable-buttons\">r>t<\"F\"ip>";
    datatable_config.fnServerData = get_datatable_request_function({}, filter_div);

    return datatable_config;
}

