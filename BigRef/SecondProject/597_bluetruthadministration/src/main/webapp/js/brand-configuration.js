function init_brand_configuration(brandName){
    $("#brand-name-crumb").text(brandName);
    create_email_table(brandName);
    create_new_email_button();
    create_phone_table(brandName);
    create_new_phone_button();
}

function create_email_table(brandName){
    var table = $("#brand-email-table");
    var filter_div = $("<div/>");
    var table_headers = $("th", table);
    var brand_email_datatable_config = create_brand_email_datatable_config(filter_div, table, brandName);
    datatables['brand-email-table'] = table.dataTable(brand_email_datatable_config);
    var datatable = datatables['brand-email-table'];
    
    $(".datatable-buttons", table.closest(".dataTables_wrapper"))
    .append(create_show_hide_column_button({
        dialog:$("#brand-email-show-hide-column-dialog"),
        headers:table_headers,
        datatable:datatable,
        aoColumnDefs:brand_email_datatable_config.aoColumnDefs
    }))
    .append(create_filter_button({
        dialog:$("#brand-email-filter-dialog"),
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
        },
        {
            "target":2,
            "type":"string"
        }
        ]
    }));
}

function create_new_email_button(){
    $(".new-brand-email-button").button().bind({
        "click":display_new_brand_email_dialog
    });
}

function display_new_brand_email_dialog(){
    var dialog = $("#add-brand-email-dialog");
    dialog.dialog({
        modal:true,
        width: "600px",
        buttons: {
            "Add new email": function(){
                var form = dialog.find("form");

                form.validate({
                    rules: {
                        brand_name:{
                            required: true
                        },
                        contact: {
                            required:true,
                            email:true
                        },
                        contact_method: {
                            required:true
                        },
                        description: {
                            required:true
                        },
                        title: {
                            required:true
                        }
                    }
                });
                if(form.valid()){
                    $.post("BrandConfigurationManager",
                        form.serialize(),
                        function(data){
                            if(data.success){
                                datatable_refresh();
                                dialog.dialog("close");
                            } else {
                                $(".message").text(data.message).addClass("error");
                            }
                        },
                        "json");
                }
            },
            "Cancel": function(){
                dialog.dialog("close");
            }
        }
    });
}

function create_phone_table(brandName){
    var table = $("#brand-phone-table");
    var filter_div = $("<div/>");
    var table_headers = $("th", table);
    var brand_phone_datatable_config = create_brand_phone_datatable_config(filter_div, table, brandName);
    datatables['brand-phone-table'] = table.dataTable(brand_phone_datatable_config);
    var datatable = datatables['brand-phone-table'];
    
    $(".datatable-buttons", table.closest(".dataTables_wrapper"))
    .append(create_show_hide_column_button({
        dialog:$("#brand-phone-show-hide-column-dialog"),
        headers:table_headers,
        datatable:datatable,
        aoColumnDefs:brand_phone_datatable_config.aoColumnDefs
    }))
    .append(create_filter_button({
        dialog:$("#brand-phone-filter-dialog"),
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
        },
        {
            "target":2,
            "type":"string"
        }
        ]
    }));
}

function create_new_phone_button(){
    $(".new-brand-phone-button").button().bind({
        "click":display_new_brand_phone_dialog
    });
}

function display_new_brand_phone_dialog(){
    var dialog = $("#add-brand-phone-dialog");
    dialog.dialog({
        modal:true,
        width: "600px",
        buttons: {
            "Add new phone": function(){
                var form = dialog.find("form");

                form.validate({
                    rules: {
                        brand_name:{
                            required: true
                        },
                        contact: {
                            required:true
                        },
                        contact_method: {
                            required:true
                        },
                        description: {
                            required:true
                        },
                        title: {
                            required:true
                        }
                    }
                });
                if(form.valid()){
                    $.post("BrandConfigurationManager",
                        form.serialize(),
                        function(data){
                            if(data.success){
                                datatable_refresh();
                                dialog.dialog("close");
                            } else {
                                $(".message").text(data.message).addClass("error");
                            }
                        },
                        "json");
                }
            },
            "Cancel": function(){
                dialog.dialog("close");
            }
        }
    });
}

function create_brand_email_datatable_config(filter_div, table, brandName){
    var datatable_config = common_datatable_configuration();
    datatable_config.aoColumnDefs = [
    {
        "sClass":"text center",
        "aTargets":[0,2]
    },
    {
        "sClass":"email center",
        "aTargets":[1]
    },
    {
        "sClass":"center",
        "aTargets":[3]
    },
    {
        "sWidth":"300px",
        "aTargets":[0,1]
    },
    {
        "sWidth":"20px",
        "aTargets":[3]
    },
    {
        "bSortable":false,
        "aTargets":[3]
    }
    ];

    datatable_config.aoColumns = [
    null,
    null,
    null,
    {
        "mDataProp":function(source, type, val){
            return "<a class='delete' title='Delete ["+source[0]+"]' message='Are you sure you wish to delete email for ["+source[0]+"]?' brandtitle='"+source[0]+"'>delete</a>";
        }
    }
    ];
    
    var jeditable_config_text = common_jeditable_configuration();
    jeditable_config_text.callback = function(sValue, y){
        var aPos = datatables['brand-email-table'].fnGetPosition(this);
        datatables['brand-email-table'].fnUpdate('Saving...', aPos[0], aPos[1]);
        var data = $.parseJSON(sValue);
        if(!data.success){
            display_information_dialog("Unable to update email table", data.message);
        } 
    };
   
    jeditable_config_text.onsubmit = function(settings, td){
        var form = $(td).find("form");
        var input = $(td).find("input");
        input.attr("name", "current-edit");
        $(form).validate({
            rules:{
                "current-edit":{
                    required: true
                }
            },
            highlight: function(element, errorClass){
                $(element).addClass("error");
            }
        });
        return ($(form).valid());
    };
    jeditable_config_text.submitdata = function(){
        return {
            brand_name: brandName,
            title: datatables['brand-email-table'].fnGetData(this.parentNode)[0],
            contact_method: "email",
            column: datatables['brand-email-table'].fnGetPosition(this)[2],
            action: "update"
        };
    };
    
    var jeditable_config_email = common_jeditable_configuration();
    jeditable_config_email.callback = function(sValue, y){
        var aPos = datatables['brand-email-table'].fnGetPosition(this);
        datatables['brand-email-table'].fnUpdate('Saving...', aPos[0], aPos[1]);
        var data = $.parseJSON(sValue);
        if(!data.success){
            display_information_dialog("Unable to update email table", data.message);
        } 
    };
   
    jeditable_config_email.onsubmit = function(settings, td){
        var form = $(td).find("form");
        var input = $(td).find("input");
        input.attr("name", "current-edit");
        $(form).validate({
            rules:{
                "current-edit":{
                    required: true,
                    email: true
                }
            },
            highlight: function(element, errorClass){
                $(element).addClass("error");
            }
        });
        return ($(form).valid());
    };
    jeditable_config_email.submitdata = function(){
        return {
            brand_name: brandName,
            title: datatables['brand-email-table'].fnGetData(this.parentNode)[0],
            contact_method: "email",
            column: datatables['brand-email-table'].fnGetPosition(this)[2],
            action: "update"
        };
    };

    datatable_config.fnDrawCallback = function(){
        $('td.text', datatables['brand-email-table'].fnGetNodes()).editable('BrandConfigurationManager', jeditable_config_text);
        $('td.email', datatables['brand-email-table'].fnGetNodes()).editable('BrandConfigurationManager', jeditable_config_email);
        create_datatable_links(datatables['brand-email-table'], {}, {
            delete_callback:function(element, callbacks){
                $.post("BrandConfigurationManager",
                {
                    action:"delete",
                    brand_name:brandName,
                    title:element.attr("brandtitle"),
                    contact_method:"email"
                },
                function(data){
                    datatable_refresh();
                })
                .error(function(){
                    alert("error");
                });
                callbacks.complete();
            }
        });
    };

    datatable_config.sAjaxSource = "BrandConfigurationDatatable";
    datatable_config.sDom = "<\"H\"l<\"datatable-buttons\">r>t<\"F\"ip>";
    datatable_config.fnServerData = get_datatable_request_function(
    {
        brandName:brandName,
        contactMethod:"email"
    }, filter_div);

    return datatable_config;
}

function create_brand_phone_datatable_config(filter_div, table, brandName){
    var datatable_config = common_datatable_configuration();
    datatable_config.aoColumnDefs = [
    {
        "sClass":"text center",
        "aTargets":[0,2]
    },
    {
        "sClass":"phone center",
        "aTargets":[1]
    },
    {
        "sClass":"center",
        "aTargets":[3]
    },
    {
        "sWidth":"300px",
        "aTargets":[0,1]
    },
    {
        "sWidth":"20px",
        "aTargets":[3]
    },
    {
        "bSortable":false,
        "aTargets":[3]
    }
    ];

    datatable_config.aoColumns = [
    null,
    null,
    null,
    {
        "mDataProp":function(source, type, val){
            return "<a class='delete' title='Delete ["+source[0]+"]' message='Are you sure you wish to delete telephone for ["+source[0]+"]?' brandtitle='"+source[0]+"'>delete</a>";
        }
    }
    ];
    
    var jeditable_config_text = common_jeditable_configuration();
    jeditable_config_text.callback = function(sValue, y){
        var aPos = datatables['brand-phone-table'].fnGetPosition(this);
        datatables['brand-phone-table'].fnUpdate('Saving...', aPos[0], aPos[1]);
        var data = $.parseJSON(sValue);
        if(!data.success){
            display_information_dialog("Unable to update phone table", data.message);
        } 
    };
   
    jeditable_config_text.onsubmit = function(settings, td){
        var form = $(td).find("form");
        var input = $(td).find("input");
        input.attr("name", "current-edit");
        $(form).validate({
            rules:{
                "current-edit":{
                    required: true
                }
            },
            highlight: function(element, errorClass){
                $(element).addClass("error");
            }
        });
        return ($(form).valid());
    };
    jeditable_config_text.submitdata = function(){
        return {
            brand_name: brandName,
            title: datatables['brand-phone-table'].fnGetData(this.parentNode)[0],
            contact_method: "phone",
            column: datatables['brand-phone-table'].fnGetPosition(this)[2],
            action: "update"
        };
    };
    
    var jeditable_config_phone = common_jeditable_configuration();
    jeditable_config_phone.callback = function(sValue, y){
        var aPos = datatables['brand-phone-table'].fnGetPosition(this);
        datatables['brand-phone-table'].fnUpdate('Saving...', aPos[0], aPos[1]);
        var data = $.parseJSON(sValue);
        if(!data.success){
            display_information_dialog("Unable to update phone table", data.message);
        } 
    };
   
    jeditable_config_phone.onsubmit = function(settings, td){
        var form = $(td).find("form");
        var input = $(td).find("input");
        input.attr("name", "current-edit");
        $(form).validate({
            rules:{
                "current-edit":{
                    required: true
                }
            },
            highlight: function(element, errorClass){
                $(element).addClass("error");
            }
        });
        return ($(form).valid());
    };
    jeditable_config_phone.submitdata = function(){
        return {
            brand_name: brandName,
            title: datatables['brand-phone-table'].fnGetData(this.parentNode)[0],
            contact_method: "phone",
            column: datatables['brand-phone-table'].fnGetPosition(this)[2],
            action: "update"
        };
    };

    datatable_config.fnDrawCallback = function(){
        $('td.text', datatables['brand-phone-table'].fnGetNodes()).editable('BrandConfigurationManager', jeditable_config_text);
        $('td.phone', datatables['brand-phone-table'].fnGetNodes()).editable('BrandConfigurationManager', jeditable_config_phone);
        create_datatable_links(datatables['brand-phone-table'], {},{
            delete_callback:function(element, callbacks){
                $.post("BrandConfigurationManager",
                {
                    action:"delete",
                    brand_name:brandName,
                    title:element.attr("brandtitle"),
                    contact_method:"phone"
                },
                function(data){
                    datatable_refresh();
                })
                .error(function(){
                    alert("error");
                });
                callbacks.complete();
            }
        });
    };

    datatable_config.sAjaxSource = "BrandConfigurationDatatable";
    datatable_config.sDom = "<\"H\"l<\"datatable-buttons\">r>t<\"F\"ip>";
    datatable_config.fnServerData = get_datatable_request_function(
    {
        brandName:brandName,
        contactMethod:"phone"
    }, filter_div);

    return datatable_config;
}