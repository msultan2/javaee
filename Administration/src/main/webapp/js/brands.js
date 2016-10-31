
// SCJS 015 START

function init_brands(){
    create_brand_table();
    create_new_brand_button();    
}

function create_brand_table(){
    var table = $("#brand-table");
    var filter_div =  $("<div/>");
    var table_headers = $("th", table);
    var brand_datatable_config = create_brand_datatable_config(filter_div);
    datatables['brand-table'] = table.dataTable(brand_datatable_config);
    var datatable = datatables['brand-table'];
    
    $(".datatable-buttons", table.closest(".dataTables_wrapper"))
    .append(create_csv_download_button({
        filter_div:filter_div,
        datatable:datatable,
        ajax_url:brand_datatable_config.sAjaxSource
    }))
    .append(create_show_hide_column_button({
        dialog:$("#brand-show-hide-column-dialog"),
        headers:table_headers,
        datatable:datatable,
        aoColumnDefs:brand_datatable_config.aoColumnDefs
    }))
    .append(create_filter_button({
        dialog:$("#brand-filter-dialog"),
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
        }]
    }));
}

function create_brand_datatable_config(filter_div){
    var datatable_config = common_datatable_configuration();
    datatable_config.aoColumnDefs = [
    {
        "sClass":"text",
        "aTargets":[0,1,2]
    },
    {
        "sClass":"center",
        "aTargets":[3,4]
    },
    {
        "bSortable":false,
        "aTargets":[3,4]
    },
    {
        "sWidth":"20px",
        "aTargets":[3]
    },
    {
        "sWidth":"20px",
        "aTargets":[4]
    }];

    datatable_config.aoColumns = [
    null,
    null,
    null,
    {
        "mDataProp":function(source, type, val){            
            return "<a class='configuration' title='View ["+source[0]+"] Configuration' id='"+source[0]+"' href='BrandConfiguration?brandname="+encodeURIComponent(escape(source[0]))+"'>configuration</a>";
        }
    },
    {
        "mDataProp":function(source, type, val){
            return "<a class='delete' title='Delete ["+source[0]+"]' message='Are you sure you wish to delete brand ["+source[0]+"]?' brandname='"+source[0]+"'>delete</a>";
        }
    }
    ];

    var jeditable_config_text = common_jeditable_configuration();
    jeditable_config_text.callback = function(sValue, y){
        var aPos = datatables['brand-table'].fnGetPosition(this);
        datatables['brand-table'].fnUpdate('Saving...', aPos[0], aPos[1]);
        var data = $.parseJSON(sValue);
        if(!data.success){
            display_information_dialog("Unable to update brand", data.message);
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
            brand_name: datatables['brand-table'].fnGetData(this.parentNode)[0],
            column: datatables['brand-table'].fnGetPosition(this)[2],
            action: "update"
        };
    };

    datatable_config.fnDrawCallback = function(){
        $('td.text', datatables['brand-table'].fnGetNodes()).editable('BrandManager', jeditable_config_text);

    create_datatable_links(datatables['brand-table'], {},
        {
            delete_callback:function(element, callbacks){
                $.post("BrandManager",
                {
                    action:"delete",
                    brand_name:element.attr("brandname")
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
    datatable_config.sAjaxSource = "BrandManagementDatatable";
    datatable_config.sDom = "<\"H\"l<\"datatable-buttons\">r>t<\"F\"ip>";
    datatable_config.fnServerData = get_datatable_request_function({}, filter_div);

    return datatable_config;
}

function create_new_brand_button(){
    $(".new-brand-button").button().bind({
        "click":display_new_brand_dialog
    });
}

function display_new_brand_dialog(){
    var dialog = $("#new-brand-dialog");
    dialog.dialog({
        modal:true,
        width: "600px",
        buttons: {
            "Add new brand": function(){
                var form = dialog.find("form");

                form.validate({
                    rules: {
                        brand_name:{
                            required: true
                        },
                        css_url: {
                            required:true
                        }
                    }
                });
                if(form.valid()){
                    $.post("BrandManager",
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

// SCJS 015 END