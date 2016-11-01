function init_detector_default_config() {
    create_detector_default_config_table();
    create_new_detector_default_config_button();
}

function create_detector_default_config_table() {
    var table = $("#detector-config-table");
    var filter_div = $("<div/>");
    var table_headers = $("th", table);
    var detector_default_config_datatable = create_detector_default_config_datatable(filter_div);
    datatables['detector-config-table'] = table.dataTable(detector_default_config_datatable);
    var datatable = datatables['detector-config-table'];

    $(".datatable-buttons", table.closest(".dataTables_wrapper"))
            .append(create_csv_download_button({
                filter_div: filter_div,
                datatable: datatable,
                ajax_url: detector_default_config_datatable.sAjaxSource
            }))
            .append(create_show_hide_column_button({
                dialog: $("#detector-config-show-hide-column-dialog"),
                headers: table_headers,
                datatable: datatable,
                aoColumnDefs: detector_default_config_datatable.aoColumnDefs
            }))
            .append(create_filter_button({
                dialog: $("#detector-config-filter-dialog"),
                filter_div: filter_div,
                datatable: datatable,
                datatable_headers: table_headers,
                column_filter_definitions: [{
                        "target": 0,
                        "type": "string"
                    },
                    {
                        "target": 1,
                        "type": "string"
                    }]
            }));
}

function create_detector_default_config_datatable(filter_div) {
    var datatable_config = common_datatable_configuration();
    datatable_config.aoColumnDefs = [
        {
            "sClass": "text",
            "aTargets": [0, 1]
        }
    ];

    datatable_config.aoColumns = [
        null,
        null,
        {
            "mDataProp": function (source, type, val) {
                return "<a class='delete' title='Delete [" + source[0] + "]' message='Are you sure you wish to delete detector default configuration [" + source[0] + "]?' property='" + source[0] + "'>delete</a>";
            }
        }
    ];

    var jeditable_config_text = common_jeditable_configuration();
    jeditable_config_text.callback = function (sValue, y) {
        var aPos = datatables['detector-config-table'].fnGetPosition(this);
        datatables['detector-config-table'].fnUpdate('Saving...', aPos[0], aPos[1]);
        var data = $.parseJSON(sValue);
        if (!data.success) {
            display_information_dialog("Unable to display detector default config", data.message);
        }
    };
    jeditable_config_text.onsubmit = function (settings, td) {
        var form = $(td).find("form");
        var input = $(td).find("input");
        input.attr("name", "current-edit");
        $(form).validate({
            rules: {
                "current-edit": {
                    required: true
                }
            },
            highlight: function (element, errorClass) {
                $(element).addClass("error");
            }
        });
        return ($(form).valid());
    };

    jeditable_config_text.submitdata = function () {
        return {
            property: datatables['detector-config-table'].fnGetData(this.parentNode)[0],
            column: datatables['detector-config-table'].fnGetPosition(this)[2],
            action: "update"
        };
    };

    datatable_config.fnDrawCallback = function () {
        $('td.text', datatables['detector-config-table'].fnGetNodes()).editable('DetectorDefaultConfigurationManager', jeditable_config_text);

        create_datatable_links(datatables['detector-config-table'], {},
                {
                    delete_callback: function (element, callbacks) {
                        $.post("DetectorDefaultConfigurationManager",
                                {
                                    action: "delete",
                                    property: element.attr("property")
                                },
                        function (data) {
                            datatable_refresh();
                        })
                                .error(function () {
                                    alert("error");
                                });
                        callbacks.complete();
                    }
                });
    };
    datatable_config.sAjaxSource = "DetectorDefaultConfigurationDatatable";
    datatable_config.sDom = "<\"H\"l<\"datatable-buttons\">r>t<\"F\"ip>";
    datatable_config.fnServerData = get_datatable_request_function({}, filter_div);

    return datatable_config;
}

function create_new_detector_default_config_button() {
    $(".new-detector-config-button").button().bind({
        "click": display_new_detector_default_config_dialog
    });
}

function display_new_detector_default_config_dialog() {
    var dialog = $("#new-detector-config-dialog");
    dialog.dialog({
        modal: true,
        width: "600px",
        buttons: {
            "Add new detector default configuration": function () {
                var form = dialog.find("form");

                form.validate({
                    rules: {
                        property: {
                            required: true
                        },
                        value: {
                            required: true
                        }
                    }
                });
                if (form.valid()) {
                    $.post("DetectorDefaultConfigurationManager",
                            form.serialize(),
                            function (data) {
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
            "Cancel": function () {
                dialog.dialog("close");
            }
        }
    });
}