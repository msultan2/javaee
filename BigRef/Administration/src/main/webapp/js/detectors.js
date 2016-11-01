function init_detector() {
    $("#detector-link").attr("href", "#");
    create_detector_table();
    create_new_detector_button();
    create_unconfigured_detector_table();
}

function create_unconfigured_detector_table() {

    var table = $("#unconfigured-detector-table");
    var filter_div = $("<div/>");
    var table_headers = $("th", table);
    var unconfigured_detector_datatable_config = create_unconfigured_detector_datatable_config(filter_div, table);
    datatables['unconfigured-detector-table'] = table.dataTable(unconfigured_detector_datatable_config);
    var datatable = datatables['unconfigured-detector-table'];

    $(".datatable-buttons", table.closest(".dataTables_wrapper"))
            .append(create_csv_download_button({
                filter_div: filter_div,
                datatable: datatable,
                ajax_url: unconfigured_detector_datatable_config.sAjaxSource
            }))
            .append(create_show_hide_column_button({
                dialog: $("#unconfigured-detector-show-hide-column-dialog"),
                headers: table_headers,
                datatable: datatable,
                aoColumnDefs: unconfigured_detector_datatable_config.aoColumnDefs
            }))
            .append(create_filter_button({
                dialog: $("#unconfigured-detector-filter-dialog"),
                filter_div: filter_div,
                datatable: datatable,
                datatable_headers: table_headers,
                column_filter_definitions: [{
                        "target": 0,
                        "type": "string"
                    },
                    {
                        "target": 1,
                        "type": "date"
                    },
                    {
                        "target": 2,
                        "type": "date"
                    },
                    {
                        "target": 3,
                        "type": "date"
                    },
                    {
                        "target": 4,
                        "type": "date"
                    },
                    {
                        "target": 5,
                        "type": "date"
                    }
                ]
            }));
}

function create_unconfigured_detector_datatable_config(filter_div, table) {
    var datatable_config = common_datatable_configuration();
    datatable_config.aoColumnDefs = [
        {
            "sClass": "text center",
            "aTargets": [0, 1, 2, 3, 4, 5]
        },
        {
            "sClass": "center",
            "aTargets": [6, 7]
        },
        {
            "bSortable": false,
            "aTargets": [6, 7]
        },
        {
            "sWidth": "200px",
            "aTargets": [0]
        },
        {
            "sWidth": "20px",
            "aTargets": [7]
        },
        {
            "sWidth": "100px",
            "aTargets": [6]
        }
    ];

    datatable_config.aoColumns = [
        null,
        null,
        null,
        null,
        null,
        null,
        {
            "mDataProp": function(source, type, val) {
                return "<a class='add-unconfig-detector' title='Add [" + source[0] + "]' detectorId='" + source[0] + "''> Add </a>";
            }
        },
        {
            "mDataProp": function(source, type, val) {
                return "<a class='delete' title='Delete [" + source[0] + "]' message='Are you sure you wish to delete detector [" + source[0] + "]?' id='" + source[0] + "'>delete</a>";
            }
        }
    ];
    datatable_config.fnDrawCallback = function() {
        create_datatable_links(datatables['unconfigured-detector-table'], {},
                {
                    delete_callback: function(element, callbacks) {
                        $.post("DetectorManager",
                                {
                                    action: "delete-unconfig",
                                    id: element.attr("id")
                                },
                        function(data) {
                            datatable_refresh();
                        })
                                .error(function() {
                                    alert("error");
                                });

                        callbacks.complete();
                    }
                });
    }

    datatable_config.sAjaxSource = "UnconfiguredDetectorDatatable";
    datatable_config.sDom = "<\"H\"l<\"datatable-buttons\">r>t<\"F\"ip>";
    datatable_config.fnServerData = get_datatable_request_function({}, filter_div);

    return datatable_config;
}

function create_detector_table() {

    var logical_group_index = 8;
    var recordType = "Detectors";
    var table = $("#detector-table");
    var filter_div = $("<div/>");
    var table_headers = $("th", table);
    var detector_datatable_config = create_detector_datatable_config(filter_div);
    datatables['detector-table'] = table.dataTable(detector_datatable_config);
    var datatable = datatables['detector-table'];

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
        $(".datatable-buttons", table.closest(".dataTables_wrapper"))
                .append(create_csv_download_button({
                    filter_div: filter_div,
                    datatable: datatable,
                    ajax_url: detector_datatable_config.sAjaxSource
                }))
                .append(create_show_hide_column_button({
                    dialog: $("#detector-show-hide-column-dialog"),
                    headers: table_headers,
                    datatable: datatable,
                    aoColumnDefs: detector_datatable_config.aoColumnDefs
                }))
                .append(create_filter_button({
                    dialog: $("#detector-filter-dialog"),
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
                            "type": "numeric"
                        },
                        {
                            "target": 4,
                            "type": "numeric"
                        },
                        {
                            "target": 5,
                            "type": "string"
                        },
                        {
                            "target": 6,
                            "type": "string"
                        },
                        {
                            "target": 7,
                            "type": "boolean"
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
}

function create_new_detector_button() {
    $(".new-detector-button").button().bind({
        "click": display_new_detector_dialog
    });
}

function display_new_detector_dialog() {
    $.post("CurrentUserManager",
            {
                action: "visible_logical_groups"
            },
    function(data) {
                add_logical_groups('#logical_group_names', data.data);
    },
            "json");

    $(".message").text("").removeClass("error");

    var dialog = $("#new-detector-dialog");
    dialog.dialog({
        modal: true,
        width: "600px",
        buttons: {
            "Add new detector": function() {
                var form = dialog.find("form");

                form.validate({
                    messages: {
                        logical_group_names: {
                            required: "At least one logical group must be selected."
                        }
                    }
                });

                if (form.valid()) {
                    $.post("DetectorManager",
                            form.serialize(),
                            function(data) {
                                if (data.success) {
                                    datatable_refresh();
                                    dialog.dialog("close");
                                } else {
                                    $(".message").text(data.message).addClass("error");
                                }
                            },
                            "json");
                }
            },
            "Cancel": function() {
                dialog.dialog("close");
            }
        }
    });
}

function create_detector_datatable_config(filter_div) {
    var datatable_config = common_datatable_configuration();
    datatable_config.aoColumnDefs = [{
            "sClass": "text",
            "aTargets": [0]
        },
        {
            "sClass": "text center",
            "aTargets": [1, 2, 3, 4]
        },
        {
            "sClass": "mode",
            "aTargets": [5]
        },
        {
            "sClass": "center carriageway",
            "aTargets": [6]
        },
        {
            "sClass": "center active",
            "aTargets": [7]
        },
        {
            "sClass": "center",
            "aTargets": [9, 10, 11, 12]
        },
        {
            "bSortable": false,
            "aTargets": [8, 9, 10, 11, 12]
        },
        {
            "sWidth": "100px",
            "aTargets": [7, 9, 10]
        },
        {
            "sWidth": "100px",
            "aTargets": [9, 10, 11]
        }];
    datatable_config.aoColumns = [
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        {
            "mDataProp": function(source, type, val) {
                return checkbox.element(source, type, val);
            }
        },
        {
            "mDataProp": function(source, type, val) {
                return source[8].replace(/\] \[/g, "] <br/> [");
            }
        },
        {
            "mDataProp": function(source, type, val) {
                return "<a class='configuration' title='View [" + source[0] + "] Configuration' id='" + source[1] + "' href='DetectorConfiguration?id=" + encodeURIComponent(escape(source[1])) + "'>configuration</a>";
            }
        },
        {
            "mDataProp": function(source, type, val) {
                return "<a class='configuration' title='Manage [" + source[0] + "]' detectorName='" + source[0] + "' href='DetectorManage?detectorName=" + encodeURIComponent(escape(source[0])) + "&detectorId=" + encodeURIComponent(escape(source[1])) + "'>manage</a>";
            }
        },
        {
            "mDataProp": function(source, type, val) {
                return "<a class='view' title='View [" + source[0] + "]' detectorName='" + source[0] + "' href='Detector?detectorName=" + encodeURIComponent(escape(source[0])) + "&detectorId=" + encodeURIComponent(escape(source[1])) + "'>view data</a>";
            }
        },
        {
            "mDataProp": function(source, type, val) {
                return "<a class='delete' title='Delete [" + source[0] + "]' message='Are you sure you wish to delete detector [" + source[0] + "]?' id='" + source[1] + "'>delete</a>";
            }
        }
    ];

    var jeditable_config = common_jeditable_configuration();
    jeditable_config.callback = function(sValue, y) {
        var aPos = datatables['detector-table'].fnGetPosition(this);
        datatables['detector-table'].fnUpdate(sValue, aPos[0], aPos[1]);
        var data = $.parseJSON(sValue);
        if (!data.success) {
            display_information_dialog("Unable to update detector", data.message);
        }
    };
    jeditable_config.onsubmit = function(settings, td) {
        var form = $(td).find("form");
        var input = $(td).find("input");
        input.attr("name", "current-edit");
        $(form).validate({
            rules: {
                "current-edit": {
                    required: true
                }
            },
            highlight: function(element, errorClass) {
                $(element).addClass("error");
            }
        });
        return ($(form).valid());
    };
    jeditable_config.submitdata = function() {
        return {
            id: datatables['detector-table'].fnGetData(this.parentNode)[1],
            column: datatables['detector-table'].fnGetPosition(this)[2],
            action: "update"
        };
    };

    var jeditable_config_mode = common_jeditable_configuration();
    jeditable_config_mode.callback = function(sValue, y) {
        var aPos = datatables['detector-table'].fnGetPosition(this);
        datatables['detector-table'].fnUpdate(sValue, aPos[0], aPos[1]);
        var data = $.parseJSON(sValue);
        if (!data.success) {
            display_information_dialog("Unable to update detector", data.message);
        }
    };
    jeditable_config_mode.data = "{0:'MODE 0 - Idle',1:'MODE 1 - Journey Time',2:'MODE 2 - Occupancy',3:'MODE 3 - Journey Time & Occupancy'}";
    jeditable_config_mode.type = "select";
    jeditable_config_mode.submitdata = function() {
        return {
            id: datatables['detector-table'].fnGetData(this.parentNode)[1],
            column: datatables['detector-table'].fnGetPosition(this)[2],
            action: "update"
        };
    };

    var jeditable_config_carriageway = common_jeditable_configuration();
    jeditable_config_carriageway.callback = function(sValue, y) {
        var aPos = datatables['detector-table'].fnGetPosition(this);
        datatables['detector-table'].fnUpdate(sValue, aPos[0], aPos[1]);
        var data = $.parseJSON(sValue);
        if (!data.success) {
            display_information_dialog("Unable to update detector", data.message);
        }
    };
    jeditable_config_carriageway.data = "{'North':'North','South':'South','East':'East','West':'West'}";
    jeditable_config_carriageway.type = "select";
    jeditable_config_carriageway.submitdata = function() {
        return {
            id: datatables['detector-table'].fnGetData(this.parentNode)[1],
            column: datatables['detector-table'].fnGetPosition(this)[2],
            action: "update"
        };
    };

    datatable_config.fnDrawCallback = function() {
        $('td.text', datatables['detector-table'].fnGetNodes()).editable('DetectorManager', jeditable_config);
        $('td.mode', datatables['detector-table'].fnGetNodes()).editable('DetectorManager', jeditable_config_mode);
        $('td.carriageway', datatables['detector-table'].fnGetNodes()).editable('DetectorManager', jeditable_config_carriageway);
        $('a.view', datatables['detector-table'])
                .button({
                    icons: {
                        primary: "ui-icon-search"
                    }
                })
                .addClass("ui-state-highlight");
        create_datatable_links(datatables['detector-table'], {},
                {
                    delete_callback: function(element, callbacks) {
                        $.post("DetectorManager",
                                {
                                    action: "delete",
                                    id: element.attr("id")
                                },
                        function(data) {
                            datatable_refresh();
                        })
                                .error(function() {
                                    alert("error");
                                });

                        callbacks.complete();
                    }
                });
    }

    datatable_config.sAjaxSource = "DetectorDatatable";
    datatable_config.sDom = "<\"H\"l<\"datatable-buttons\">r>t<\"F\"ip>";
    datatable_config.fnServerData = get_datatable_request_function({}, filter_div);

    return datatable_config;
}

function display_unconfig_detector_dialog(detectorId) {

    $("#id").val(detectorId);
    $.post("CurrentUserManager",
            {
                action: "visible_logical_groups"
            },
    function(data) {
        $("#logical-group-select").empty();
        for (var i = 0; i < data.data.length; i++) {
            $("#logical-group-select").append($("<div/>")
                    .append($("<label/>").text(data.data[i]))
                    .append($("<input type='checkbox' checked='checked'>")
                            .val(data.data[i])
                            .attr("name", "logical_group_names")
                            .addClass("required"))
                    );
        }
        $("#logical-group-select").append($("<hr/>").addClass("hr-clear-float"));
    },
            "json");

    $(".message").text("").removeClass("error");

    var dialog = $("#new-detector-dialog");
    dialog.dialog({
        modal: true,
        width: "600px",
        buttons: {
            "Add new detector": function() {
                var form = dialog.find("form");

                form.validate({
                    messages: {
                        logical_group_names: {
                            required: "At least one logical group must be selected."
                        }
                    }
                });

                if (form.valid()) {
                    $.post("DetectorManager",
                            form.serialize(),
                            function(data) {
                                if (data.success) {
                                    datatable_refresh();
                                    dialog.dialog("close");
                                } else {
                                    $(".message").text(data.message).addClass("error");
                                }
                            },
                            "json");
                }
            },
            "Cancel": function() {
                dialog.dialog("close");
            }
        }
    });
}

var checkbox = {
    element: function(source, type, original) {
        var input = '<input type="checkbox" id =' + source[1] + ' name="active" onclick="handleActiveCheckbox(this);"';

        if (source[7] === "true") {
            input = input + " value='true' checked>";
        } else {
            input = input + " value='false'>";
        }

        return input;
    }
};

function handleActiveCheckbox(input) {
    $.post("DetectorManager",
            {
                action: "update",
                id: input.id,
                column: 7,
                value: input.checked
            },
    function(data) {
        datatable_refresh();
    })
    .error(function() {
        alert("error updating active map icons field");
    });
}

