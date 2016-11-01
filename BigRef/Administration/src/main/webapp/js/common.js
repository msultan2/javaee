var json_error_in_progress = false;
var datatables = new Array();
var datatable_refresh_interval = 0;
var common_datatable_configuration = function(){
    return {
        bAutoWidth:false,
        bJQueryUI: true,
        sPaginationType: "full_numbers",
        bServerSide: true,
        bFilter: false,
        aaSorting:[[0,"asc"]],
        iDisplayLength: 10,
        bStateSave:true,
        fnServerData: function ( sSource, aoData, fnCallback ) {
            $.ajax({
                "dataType": 'json',
                "type": "POST",
                "url": sSource,
                "data": aoData,
                "success": fnCallback
            });
        }
    }
};

function start_datatable_refresh_interval() {
    stop_datatable_refresh_interval();
    setInterval(function(){
        datatable_refresh();
    }, 60000);
}

function datatable_refresh(){
    for(var key in datatables){
        datatables[key].fnDraw(false);
    }
}

function stop_datatable_refresh_interval(){
    clearInterval(datatable_refresh_interval);
}


var common_jeditable_configuration = function(){
    return {
        style:"inherit",
        cssclass: "jeditable",
        submit:"Confirm",
        cancel:"Cancel",
        indicator:"Saving...",
        placeholder:"Click to edit...",
        tooltip: "Click to edit...",
        height:"15px",
        width:"200px",
        event: "click"
    }
};

function format_interval_element(val, unit){
    if(val != "00" && val != "0"){
        var i = parseInt(val.replace(/^[0]+/g,""));
        var plural ="";
        if(i > 1){
            plural = "s";
        }
        return i+" "+unit+plural+" ";
    }
    return "";
}

function format_interval_string(str){

    if(str == ""){
        return "-";
    }

    var i_array = str.split(".")[0].split(":");
    var ret_str = "";
    if(i_array.length > 3){
        ret_str += format_interval_element(i_array[0], "Year");
        ret_str += format_interval_element(i_array[1], "Month");
        ret_str += format_interval_element(i_array[2], "Day");
        ret_str += format_interval_element(i_array[3], "Hour");
        ret_str += format_interval_element(i_array[4], "Minute");
        ret_str += format_interval_element(i_array[5], "Second");
    } else {
        ret_str += format_interval_element(i_array[0], "Hour");
        ret_str += format_interval_element(i_array[1], "Minute");
        ret_str += format_interval_element(i_array[2], "Second");
    }
    return ret_str;
}

var still_on_same_page = true;
function init_common(){
    $("#common-links").buttonset();
    still_on_same_page = true;
    $(window).bind("beforeunload", function(){
        still_on_same_page = false;
    });
    $("body").ajaxError(function(){
        react_to_json_error();
    });
    keep_session_alive();
}

function keep_session_alive(){
    setInterval(function(){
        $.post("/Home");
    }, 30000);
}

function create_datatable_links(table, sConfiguration, sDelete){

    // SCJS 017 START
    $('a.add-unconfig-detector', table)
    .button({
        icons:{
            primary:"ui-icon-plusthick"
        }
    })
    .addClass("ui-state-highlight")
    .bind({
        click:function(){
            var detectorId = $(this).attr("detectorId");
            if(detectorId != undefined){
                $.post("LogicalGroupManager",
                {
                    action:"add_detector",
                    detectorId:detectorId
                },
                function(data){
                    datatables['unconfigured-detector-table'].fnDraw();
                })
            }
        }
    });
    // SCJS 017 END

    $('a.view-group', table)
    .button({
        icons:{
            primary:"ui-icon-search"
        }
    })
    .addClass("ui-state-highlight")
    .bind({
        click:function(){
            var location = "LogicalGroupMenu";
            var id = $(this).attr("logicalgroupid");
            if(id != undefined){
                location+="?group="+$(this).attr("logicalgroupid");
                window.location.href=location;
            }
        }
    });
    $('a.view-route', table)
    .button({
        icons:{
            primary:"ui-icon-search"
        }
    })
    .addClass("ui-state-highlight")
    .bind({
        click:function(){
            var location = "Route";
            var id = $(this).attr("routename");
            if(id != undefined){
                location+="?route="+encodeURIComponent(escape($(this).attr("routename")));
                window.location.href=location;
            }
        }
    });    
    $('a.view-user', table)
    .button({
        icons:{
            primary:"ui-icon-search"
        }
    })
    .addClass("ui-state-highlight")
    .bind({
        click:function(){
            var location = "InstationUser";
            var username = $(this).attr("username");
            if(username != undefined){
                location+="?username="+$(this).attr("username");
                window.location.href=location;
            }
        }
    });

    $('a.add-user', table)
    .button({
        icons:{
            primary:"ui-icon-plusthick"
        }
    })
    .addClass("ui-state-highlight")
    .bind({
        click:function(){      
            var username = $(this).attr("username");
            var logical_group_name = $(this).attr("logicalgroupname");
            if(username != undefined && logical_group_name != undefined){
                $.post("LogicalGroupManager",
                {
                    action:"add_user",
                    username:username,
                    logical_group_name:logical_group_name
                },
                function(data){
                    datatables['instation-user-logical-group-table'].fnDraw();
                })
            }
        }
    });

    $('a.activate-user', table)
    .button({
        icons:{
            primary:"ui-icon-plusthick"
        }
    })
    .addClass("ui-state-highlight")
    .bind({
        click:function(){      
            var username = $(this).attr("username");
            if(username != undefined){
                $.post("InstationUserManager",
                {
                    action:"activate_user",
                    username:username
                },
                function(data){
                    datatables['instation-user-table'].fnDraw();
                })
            }
        }
    });

    $('a.add-role', table)
    .button({
        icons:{
            primary:"ui-icon-plusthick"
        }
    })
    .addClass("ui-state-highlight")
    .bind({
        click:function(){
            var username = $(this).attr("username");
            var role_name = $(this).attr("rolename");
            if(username != undefined && role_name != undefined){
                $.post("InstationUserRoleManager",
                {
                    action:"add",
                    username:username,
                    role_name:role_name
                },
                function(data){
                    datatables['instation-user-role-table'].fnDraw();
                })
            }
        }
    });

    $('a.add-detector', table)
    .button({
        icons:{
            primary:"ui-icon-plusthick"
        }
    })
    .addClass("ui-state-highlight")
    .bind({
        click:function(){
            var detector = $(this).attr("detector");
            var logical_group_name = $(this).attr("logicalgroupname");
            if(detector != undefined && logical_group_name != undefined){
                $.post("LogicalGroupManager",
                {
                    action:"add_detector",
                    detector:detector,
                    logical_group_name:logical_group_name
                },
                function(data){
                                    datatablelegends['detector-logical-group-table'].fnDraw();
                })
            }
        }
    });

    $('a.add-span', table)
    .button({
        icons:{
            primary:"ui-icon-plusthick"
        }
    })
    .addClass("ui-state-highlight")
    .bind({
        click:function(){
            var span = $(this).attr("span");
            var logical_group_name = $(this).attr("logicalgroupname");
            if(span != undefined && logical_group_name != undefined){
                $.post("LogicalGroupManager",
                {
                    action:"add_span",
                    span:span,
                    logical_group_name:logical_group_name
                },
                function(data){
                    datatables['span-logical-group-table'].fnDraw();
                })
            }
        }
    });

    $('a.add-route-span', table)
    .button({
        icons:{
            primary:"ui-icon-plusthick"
        }
    })
    .addClass("ui-state-highlight")
    .bind({
        click:function(){
            var span = $(this).attr("span");
            var route_name = $(this).attr("routename");
            if(span != undefined && route_name != undefined){
                $.post("RouteManager",
                {
                    action:"add_span",
                    span:span,
                    route_name:route_name
                },
                function(data){
                    datatables['route-spans-table'].fnDraw();
                })
            }
        }
    });

    $('a.add-route', table)
    .button({
        icons:{
            primary:"ui-icon-plusthick"
        }
    })
    .addClass("ui-state-highlight")
    .bind({
        click:function(){
            var route = $(this).attr("route");
            var logical_group_name = $(this).attr("logicalgroupname");
            if(route != undefined && logical_group_name != undefined){
                $.post("LogicalGroupManager",
                {
                    action:"add_route",
                    route:route,
                    logical_group_name:logical_group_name
                },
                function(data){
                    datatables['route-logical-group-table'].fnDraw();
                })
            }
        }
    });

    $('a.remove-user', table)
    .button({
        icons:{
            primary:"ui-icon-minusthick"
        }
    })
    .addClass("ui-state-highlight")
    .bind({
        click:function(){
            var username = $(this).attr("username");
            var logical_group_name = $(this).attr("logicalgroupname");
            if(username != undefined && logical_group_name != undefined){
                $.post("LogicalGroupManager",
                {
                    action:"remove_user",
                    username:username,
                    logical_group_name:logical_group_name
                },
                function(data){
                    datatables['instation-user-logical-group-table'].fnDraw();
                })
            }
            
        }
    });

    $('a.remove-role', table)
    .button({
        icons:{
            primary:"ui-icon-minusthick"
        }
    })
    .addClass("ui-state-highlight")
    .bind({
        click:function(){
            var username = $(this).attr("username");
            var role_name = $(this).attr("rolename");
            if(username != undefined && role_name != undefined){
                $.post("InstationUserRoleManager",
                {
                    action:"remove",
                    username:username,
                    role_name:role_name
                },
                function(data){
                    datatables['instation-user-role-table'].fnDraw();
                })
            }

        }
    });

    $('a.remove-detector', table)
    .button({
        icons:{
            primary:"ui-icon-minusthick"
        }
    })
    .addClass("ui-state-highlight")
    .bind({
        click:function(){
            var detector = $(this).attr("detector");
            var logical_group_name = $(this).attr("logicalgroupname");
            if(detector != undefined && logical_group_name != undefined){
                $.post("LogicalGroupManager",
                {
                    action:"remove_detector",
                    detector:detector,
                    logical_group_name:logical_group_name
                },
                function(data){
                    datatables['detector-logical-group-table'].fnDraw();
                })
            }

        }
    });

    $('a.remove-span', table)
    .button({
        icons:{
            primary:"ui-icon-minusthick"
        }
    })
    .addClass("ui-state-highlight")
    .bind({
        click:function(){
            var span = $(this).attr("span");
            var logical_group_name = $(this).attr("logicalgroupname");
            if(span != undefined && logical_group_name != undefined){
                $.post("LogicalGroupManager",
                {
                    action:"remove_span",
                    span:span,
                    logical_group_name:logical_group_name
                },
                function(data){
                    datatables['span-logical-group-table'].fnDraw();
                })
            }

        }
    });

    $('a.remove-route-span', table)
    .button({
        icons:{
            primary:"ui-icon-minusthick"
        }
    })
    .addClass("ui-state-highlight")
    .bind({
        click:function(){
            var span = $(this).attr("span");
            var route_name = $(this).attr("routename");
            if(span != undefined && route_name != undefined){
                $.post("RouteManager",
                {
                    action:"remove_span",
                    span:span,
                    route_name:route_name
                },
                function(data){
                    datatables['route-spans-table'].fnDraw();
                })
            }

        }
    });

    $('a.remove-route', table)
    .button({
        icons:{
            primary:"ui-icon-minusthick"
        }
    })
    .addClass("ui-state-highlight")
    .bind({
        click:function(){
            var route = $(this).attr("route");
            var logical_group_name = $(this).attr("logicalgroupname");
            if(route != undefined && logical_group_name != undefined){
                $.post("LogicalGroupManager",
                {
                    action:"remove_route",
                    route:route,
                    logical_group_name:logical_group_name
                },
                function(data){
                    datatables['route-logical-group-table'].fnDraw();
                })
            }

        }
    });

    $('a.configuration', table)
    .button({
        icons:{
            primary:"ui-icon-document"
        }
    })
    .addClass("ui-state-highlight")
    .bind({
        click:function(){
            var location = "DetectorConfiguration";
            var id = $(this).attr("id");
            if(id != undefined){
                location+="?id="+$(this).attr("id");
                window.location.href=location;
            }
        }
    });

    // SCJS 017 START
    $('a.add-unconfig-detector', table)
    .button({
        icons:{
            primary:"ui-icon-plusthick"
        }
    })
    .addClass("ui-state-highlight")
    .bind({
        click:function(){
            var detectorId = $(this).attr("detectorId");
            if(detectorId != undefined){
                display_unconfig_detector_dialog(detectorId);
            }
        }
    });
    // SCJS 017 END

    $('a.delete', table)
    .button({
        icons:{
            primary:"ui-icon-close"
        },
        text:false
    })
    .addClass("ui-state-error")
    .bind({
        click:function(){
            var elem = $(this);
            display_confirmation_dialog(sDelete.delete_callback, elem);
        }
    });

    $('a.delete-user', table)
    .button({
        icons:{
            primary:"ui-icon-close"
        },
        text:false
    })
    .addClass("ui-state-error")
    .bind({
        click:function(){
            var username = $(this).attr("username");
            if(username != undefined){
                $.post("InstationUserManager",
                {
                    action:"delete",
                    username:username
                },
                function(data){
                    datatables['instation-user-table'].fnDraw();
                })
            }
        }
    });
    
    $('a.deactivate-user', table)
    .button({
        icons:{
            primary:"ui-icon-minusthick"
        }
    })
    .addClass("ui-state-highlight")
    .bind({
        click:function(){      
            var username = $(this).attr("username");
            if(username != undefined){
                $.post("InstationUserManager",
                {
                    action:"deactivate_user",
                    username:username
                },
                function(data){
                    datatables['instation-user-table'].fnDraw();
                })
            }
        }
    });

    $('a.delete-group', table)
    .button({
        icons:{
            primary:"ui-icon-close"
        },
        text:false
    })
    .addClass("ui-state-error")
    .bind({
        click:function(){
            var logicalGroupName = $(this).attr("logicalgroupid");
            if(logicalGroupName != undefined){
                $.post("LogicalGroupManager",
                {
                    action:"delete",
                    logical_group_name:logicalGroupName
                },
                function(data){
                    datatables['logical-group-table'].fnDraw();
                })
            }
        }
    });
}

function display_confirmation_dialog(callback, element){
    var dialog = $("<div/>").append($("<p/>").text(element.attr("message")));
    dialog.dialog({
        title:element.attr("title") + " Confirmation",
        modal:true,
        width:"600px",
        buttons: {
            "Confirm": function(){
                callback(element, {
                    complete:function(){
                        dialog.dialog("destroy");
                    }
                });
            },
            "Cancel": function(){
                dialog.dialog("destroy");
            }
        }
    });
}

function display_information_dialog(title, message, callback){
    var dialog = $("<div/>").append($("<p/>").text(message));
    dialog.dialog({
        title:title,
        modal:true,
        width:"600px",
        buttons: {
            "OK": function(){
                if(callback != undefined){
                    callback();
                }
                dialog.dialog("destroy");
            }
        }
    });
}

//UTILITY FUNCTIONS
function bool(v){
    if(v == null){
        return false;
    }
    return v.toString().toLowerCase() === true.toString().toLowerCase();
}

function react_to_json_error(){
    if(!json_error_in_progress && still_on_same_page){
        json_error_in_progress = true;
        //alert("Connection to the server has been lost. \nThis could be due to your session becoming invalid.");
        //window.location="/";
    }
}

function create_show_hide_column_button(params){
    var headers = params.headers;
    var no_of_headers = headers.length;
    var hidden_columns = {};
    if(params.aoColumnDefs != undefined){
        for(var i=0;i<params.aoColumnDefs.length;i++){
            if(params.aoColumnDefs[i].bVisible == false){
                for(var j=0;j<params.aoColumnDefs[i].aTargets.length;j++){
                    hidden_columns[params.aoColumnDefs[i].aTargets[j]] = true;
                }
            }
        }
    }
    headers.each(function(index){
        var checked = true;
        if(index in hidden_columns ||
            (index - no_of_headers) in hidden_columns){
            checked = false;
        }
        var attr = {
            "type":"checkbox",
            "value":index
        }
        if(checked){
            attr["CHECKED"] = "CHECKED";
        }
        params.dialog.append($("<div/>")
            .append($("<label/>")
                .text($(this).text())
                .prepend($("<input/>")
                    .attr(attr)
                    )
                )
            );
    });
    $("input", params.dialog).bind("click", function(){
        params.datatable.fnSetColumnVis($(this).val(), $(this).is(":checked"));
    });
    return $("<button/>").text("Show/Hide Columns").button().bind({
        "click":function(){
            params.dialog.dialog({
                width: "600px",
                buttons: {
                    "Close": function(){
                        params.dialog.dialog("close");
                    }
                }
            });
        }
    });
}

function create_filter_button(params) {

    var dialog = params.dialog;
    var filter = params.filter_div;
    var columnFilters = params.column_filter_definitions;
    var columnHeaders = params.datatable_headers;
    var table = params.datatable;
    var table_id = params.datatable.attr("id");
    var logical_group_index = params.logical_group_index;
    var recordType = params.recordType;

    dialog.append(
        filter.addClass("filters")
        .append($("<div/>").addClass("option-filter-section").append($("<h2/>").text("Options")))
        .append($("<div/>").addClass("string-filter-section").append($("<h2/>").text("Text")))
        .append($("<div/>").addClass("numeric-filter-section").append($("<h2/>").text("Numeric")))
        .append($("<div/>").addClass("time-filter-section").append($("<h2/>").text("Times")))
        .append($("<div/>").addClass("date-filter-section").append($("<h2/>").text("Dates")))
        .append($("<div/>").addClass("enum-filter-section").append($("<h2/>").text("Selections")))
        .append($("<div/>").addClass("boolean-filter-section").append($("<h2/>").text("Boolean")))
        );

    var option_filter = $(".option-filter-section", filter);
    var string_filter = $(".string-filter-section", filter);
    var numeric_filter = $(".numeric-filter-section", filter);
    var date_filter = $(".date-filter-section", filter);
    var time_filter = $(".time-filter-section", filter);
    var enum_filter = $(".enum-filter-section", filter);
    var boolean_filter = $(".boolean-filter-section", filter);

    for(var i = 0; i < columnFilters.length; i++){
        if (columnFilters[i] == null) {
            continue;
        }
        var value = null;
        var columnFilterType = null;
        if(columnFilters[i].value != undefined){
            value = columnFilters[i].value;
        }
        columnFilterType = columnFilters[i].type;
        if(columnFilterType == "string"){
            string_filter
            .append($("<div/>")
                .append($("<label/>")
                    .text(columnHeaders.eq(columnFilters[i].target).text()+":"))
                .append($("<input/>").val(value)
                    .attr({
                        "id":table_id+"-filter-string-"+columnFilters[i].target,
                        "index":columnFilters[i].target
                    })
                    .addClass("datatable-column-filter datatable-column-filter-value datatable-column-filter-string ui-corner-all ui-widget-content string-"+columnHeaders.eq(columnFilters[i].target).text().replace(" ","")))
                );
        } else if (columnFilterType == "numeric"
            || columnFilterType == "short"
            || columnFilterType == "integer"
            || columnFilterType == "long"){
            var numeric_min_id = table_id+"-filter-numeric-min-"+columnFilters[i].target;
            var numeric_max_id = table_id+"-filter-numeric-max-"+columnFilters[i].target;

            numeric_filter
            .append($("<div/>")
                .append($("<label/>")
                    .text(columnHeaders.eq(columnFilters[i].target).text()+" (MIN):"))
                .append($("<input/>")
                    .attr({
                        "id":numeric_min_id,
                        "index":columnFilters[i].target
                    })
                    .addClass("datatable-column-filter datatable-column-filter-value datatable-column-filter-numeric-min ui-corner-all ui-widget-content numeric-min-"+columnHeaders.eq(columnFilters[i].target).text().replace(" ",""))
                    .bind("keyup", columnFilters[i], remove_illegal_characters)
                    )
                )
            .append($("<div/>")
                .append($("<label/>")
                    .text(columnHeaders.eq(columnFilters[i].target).text()+" (MAX):"))
                .append($("<input/>")
                    .attr({
                        "id":numeric_max_id,
                        "index":columnFilters[i].target
                    })
                    .addClass("datatable-column-filter datatable-column-filter-value datatable-column-filter-numeric-max ui-corner-all ui-widget-content numeric-max-"+columnHeaders.eq(columnFilters[i].target).text().replace(" ",""))
                    .bind("keyup", columnFilters[i], remove_illegal_characters)
                    )
                );
            if (columnFilters[i].format == "two_digit_hex") {
                $("#" + numeric_min_id, numeric_filter).addClass("hex");
                $("#" + numeric_max_id, numeric_filter).addClass("hex");
            } else if (columnFilters[i].format == "three_digit_octal") {
                $("#" + numeric_min_id, numeric_filter).addClass("octal");
                $("#" + numeric_max_id, numeric_filter).addClass("octal");
            }
        } else if(columnFilterType == "time"){
            time_filter
            .append($("<div/>")
                .append($("<label/>")
                    .text(columnHeaders.eq(columnFilters[i].target).text()+" (MIN):"))
                .append($("<input/>")
                    .attr({
                        "id":table_id+"-filter-time-min-"+columnFilters[i].target,
                        "index":columnFilters[i].target
                    })
                    .addClass("datatable-column-filter datatable-column-filter-value datatable-column-filter-time-min ui-corner-all ui-widget-content time-min-"+columnHeaders.eq(columnFilters[i].target).text().replace(" ",""))
                    .timepicker({
                        timeFormat: 'h:mm:ss',
                        showSecond: true,
                        onClose: function(){
                            table.fnDraw();
                        }
                    }).attr("readonly","readonly")
                    )
                .append($("<label/>")
                    .text(columnHeaders.eq(columnFilters[i].target).text()+" (MAX):"))
                .append($("<input/>")
                    .attr({
                        "id":table_id+"-filter-time-max-"+columnFilters[i].target,
                        "index":columnFilters[i].target
                    })
                    .addClass("datatable-column-filter datatable-column-filter-value datatable-column-filter-time-max ui-corner-all ui-widget-content time-max-"+columnHeaders.eq(columnFilters[i].target).text().replace(" ",""))
                    .timepicker({
                        timeFormat: 'h:mm:ss',
                        showSecond: true,
                        onClose: function(){
                            table.fnDraw();
                        }
                    }).attr("readonly","readonly")
                    )
                );
        }else if(columnFilterType == "date"){
            date_filter
            .append($("<div/>")
                .append($("<label/>")
                    .text(columnHeaders.eq(columnFilters[i].target).text()+" (MIN):"))
                .append($("<input/>")
                    .attr({
                        "id":table_id+"-filter-date-min-"+columnFilters[i].target,
                        "index":columnFilters[i].target
                    })
                    .addClass("datatable-column-filter datatable-column-filter-value datatable-column-filter-date-min ui-corner-all ui-widget-content date-min-"+columnHeaders.eq(columnFilters[i].target).text().replace(" ",""))
                    .datetimepicker({
                        dateFormat: "yy-mm-dd",
                        timeFormat: "hh:mm:ss",
                        showSecond:true,
                        onClose: function(){
                            table.fnDraw();
                        }
                    })
                    )
                .append($("<label/>")
                    .text(columnHeaders.eq(columnFilters[i].target).text()+" (MAX):"))
                .append($("<input/>")
                    .attr({
                        "id":table_id+"-filter-date-max-"+columnFilters[i].target,
                        "index":columnFilters[i].target
                    })
                    .addClass("datatable-column-filter datatable-column-filter-value datatable-column-filter-date-max ui-corner-all ui-widget-content date-max-"+columnHeaders.eq(columnFilters[i].target).text().replace(" ",""))
                    .datetimepicker({
                        dateFormat: "yy-mm-dd",
                        timeFormat: "hh:mm:ss",
                        showSecond:true,
                        onClose: function(){
                            table.fnDraw();
                        }
                    })
                    )
                );
        }else if(columnFilterType == "enum"){
            enum_filter
            .append($("<div/>")
                .append($("<label/>")
                    .text(columnHeaders.eq(columnFilters[i].target).text()+":"))
                .append(enumeration_selection($("<input/>"), table, columnHeaders.eq(columnFilters[i].target).text(), columnFilters[i].options)
                    .attr({
                        "id":table_id+"-filter-enum-"+columnFilters[i].target,
                        "index":columnFilters[i].target,
                        "readonly":"readonly"
                    })
                    .addClass("datatable-column-filter datatable-column-filter-enum datatable-column-filter-logic-"+columnFilters[i].target+" datatable-column-filter-enum ui-corner-all ui-widget-content enum-"+columnHeaders.eq(columnFilters[i].target).text().replace(" ",""))
                    )
                );
        }else if(columnFilterType == "boolean"){
            var field_name = columnHeaders.eq(columnFilters[i].target).text()+"-"+columnFilters[i].target;
            var radio_btn_html = "<input type=\"radio\" name=\"" + field_name + "\"/>";
            boolean_filter
            .append($("<div/>")
                .append($("<label/>")
                    .text(columnHeaders.eq(columnFilters[i].target).text()+":"))
                .append($(radio_btn_html)
                    .attr({
                        "id":table_id+"-filter-boolean-show-"+columnFilters[i].target,
                        "index":columnFilters[i].target,
                        "value":"true"
                    })
                    .addClass("datatable-column-filter datatable-column-filter-value datatable-column-filter-boolean bool-show-"+columnHeaders.eq(columnFilters[i].target).text().replace(" ","")))
                .append($("<label/>").addClass("auto-width")
                    .text("Show"))
                .append($(radio_btn_html)
                    .attr({
                        "id":table_id+"-filter-boolean-hide-"+columnFilters[i].target,
                        "index":columnFilters[i].target,
                        "value":"false"
                    })
                    .addClass("datatable-column-filter datatable-column-filter-boolean bool-hide-"+columnHeaders.eq(columnFilters[i].target).text().replace(" ","")))
                .append($("<label/>").addClass("auto-width")
                    .text("Hide"))
                .append($(radio_btn_html)
                    .attr({
                        "id":table_id+"-filter-boolean-none-"+columnFilters[i].target,
                            "defaultChecked": true,
                            "checked":"checked",
                            "value":""
                    })
                    .addClass("datatable-column-filter datatable-column-filter-boolean bool-none bool-none-"+columnHeaders.eq(columnFilters[i].target).text().replace(" ","")+" boolean-filter-reset"))
                .append($("<label/>").addClass("auto-width")
                    .text("No Filter"))
                );
        }
    }

    var type_radio_btn_html = "<input type=\"radio\" name=\""+table.attr("id")+"-bFilterOr\"/>";

    option_filter
    .append($("<div/>")
        .append($("<label/>")
            .text("Filter Type:"))
        .append($(type_radio_btn_html)
            .attr({
                "id":table_id+"-bFilterOr-AND",
                "defaultChecked": true,
                "value":"true",
                "checked":"checked",
                "index":"filter_type"
            })
            .addClass("datatable-column-filter datatable-column-filter-and boolean-filter-reset"))
        .append($("<label/>").addClass("auto-width").attr("for", "bFilterOr-AND")
            .text("AND (Include  results that match all of the criteria)"))
        .append($(type_radio_btn_html)
            .attr({
                "id":table_id+"-bFilterOr-OR",
                "value":"false",
                "index":"filter_type"
            })
            .addClass("datatable-column-filter datatable-column-filter-or"))
        .append($("<label/>").addClass("auto-width").attr("for", "bFilterOr-OR")
            .text("OR (Include results that match any of the criteria)"))
        );


    if ($("label", string_filter).length < 1){
        string_filter.remove();
    }
    if ($("label", numeric_filter).length < 1){
        numeric_filter.remove();
    }
    if ($("label", date_filter).length < 1){
        date_filter.remove();
    }
    if ($("label", time_filter).length < 1){
        time_filter.remove();
    }
    if ($("label", enum_filter).length < 1){
        enum_filter.remove();
    }
    if ($("label", boolean_filter).length < 1){
        boolean_filter.remove();
    }

    if(dialog != undefined){

        var datatable_column_ref = $(".datatable-column-filter", filter)

        datatable_column_ref.keyup(function () {
        // QUICK FILTER MODIFIED START
         if($('input.datatable-column-filter-value[value!=""]', dialog).length > 0){
             updateQuickFilter(table);
         } else {
             resetQuickFilter(table, recordType);
         }
        // QUICK FILTER MODIFIED END

        table.fnDraw(true);
        });

        datatable_column_ref.each(function(){
            if ($(this).hasClass("datatable-column-filter-boolean")) {
                $(this).bind("change", function () {
                    table.fnDraw(true);
                });
            }
            if ($(this).hasClass("datatable-column-filter-enum")) {
                $(this).bind("change", function () {
                    table.fnDraw(true);
                });
            }
            if ($(this).hasClass("datatable-column-filter-and")) {
                $(this).bind("change", function () {
                    table.fnDraw(true);
                    });
            }
            if ($(this).hasClass("datatable-column-filter-or")) {
                $(this).bind("change", function () {
                    table.fnDraw(true);
                    });
            }
        });
    }

    dialog.dialog({
        width: "800px",
        autoOpen:false,
        buttons: {
            "Clear Filter": function(){
                // boolean-filter-reset class resets boolean checks.
                $("input[class*='boolean-filter-reset']").attr({"checked":"checked"});
                $("input[type!=radio]", dialog).val("");
                // QUICK FILTER MODIFIED START
                $("input[type=checkbox]:checked", dialog).removeAttr('checked');
                resetQuickFilter(table, recordType);
                // QUICK FILTER MODIFIED END
                table.fnDraw(true);
            },
            "Close": function(){
                // QUICK FILTER MODIFIED START
                if(($('input.datatable-column-filter-value[value!=""]', dialog).length > 0) || ($('input.datatable-column-filter-enum[value!=""]', dialog).length > 1)){
                    updateQuickFilter(table);
                } else if($('input.datatable-column-filter-logic-'+logical_group_index+'[value!=""]', dialog).length == 1){
                    var filter_string = $('input.datatable-column-filter-logic-'+logical_group_index+'[value!=""]', dialog).val();
                    if(filter_string.indexOf(',')== -1){
                        var updated_filter_string = "Displaying "+ recordType +" in " +  filter_string;
                        $("#"+table.attr("id")+"-quick-filter-select option")
                            .filter(function() {return $.trim( $(this).text()) == updated_filter_string;})
                            .attr('selected',true);
                    } else {
                        updateQuickFilter(table, recordType);
                    }
                }else if($('input.datatable-column-filter-enum[value!=""]', dialog).length == 1){
                    updateQuickFilter(table);
                }
                else{
                    resetQuickFilter(table, recordType);
                }
                // QUICK FILTER MODIFIED END
                dialog.dialog("close");
            }
        }
    });

    return  $("<button/>").text("Filter").button().click(function() {
        if(dialog.dialog("isOpen")){
            dialog.dialog("close");
        } else {
            dialog.dialog("open");
        }
    });
}

function get_datatable_request_function(parameters, filter) {
    return function(sSource, aoData, fnCallback) {
        if (typeof parameters === 'function') {
            var paramsFromFunction = parameters();
            if (paramsFromFunction === false) {
                fnCallback({
                    aaData: [],
                    iTotalDisplayRecords: 0,
                    iTotalRecords: 0
                });
                return;
            } else {
                for (key in paramsFromFunction) {
                    aoData.push({
                        name:key,
                        value:paramsFromFunction[key]
                    });
                }
            }
        } else if (parameters !== undefined) {
            for (key in parameters) {
                var val = parameters[key];
                if ($.isArray(val)) {
                    for (var i = 0; i < val.length; i++) {
                        aoData.push({
                            name:key,
                            value:val[i]
                        });
                    }
                } else {
                    aoData.push({
                        name:key,
                        value:val
                    });
                }
            }
        }

        /* Column filtering
         *
         * For all columns (including hidden columns) specify bFilterFlag_<column>
         * either true or false. This indicates whether the column should be filtered
         * or not.
         * For string type columns, a parameter sSearch_<column>, containing the
         * string text to be searched for.
         *
         * For numeric or date columns, two or four parameters:
         * sFilterMin_<column>: The minimum value for the filter
         * sFilterMax_<column>: The maximum value for the filter
         * bFilterMinIncl_<column>: Whether the minimum value is inclusive or not
         * bFilterMaxIncl_<column>: Whether the maximum value is inclusive or not
         *
         * For boolean columns, sSearch_<column>, specifying either true or false
         *
         * For enumeration columns, sSearch_<column>, containing a comma-seperated list of
         * values to include
         */



        var column_filters = $(".datatable-column-filter", filter);

        column_filters.each(function() {
            var index = $(this).attr("index");
            
            if ($(this).val() == "" || ($(this).is("[type=radio]") && !$(this).is(":checked")) || index == undefined) {
            
            } else {
                if ($(this).hasClass("datatable-column-filter-and")) {
                    if($(this).is(":checked")){
                        aoData.push({
                            name: "bFilterOr",
                            value: false
                        });
                    }
                } else if ($(this).hasClass("datatable-column-filter-or")) {
                    if($(this).is(":checked")){
                        aoData.push({
                            name: "bFilterOr",
                            value: true
                        });
                    }
                } else {
                    aoData.push({
                        name: "bFilterFlag_" + index,
                        value: "true"
                    });
                    if ($(this).hasClass("datatable-column-filter-string")) {
                        aoData.push({
                            name: "sSearch_" + index,
                            value: $(this).val()
                        });
                    } else if ($(this).hasClass("datatable-column-filter-numeric-min")) {
                        if ($(this).hasClass("hex")) {
                            aoData.push({
                                name: "sFilterMin_" + index,
                                value: "h" + $(this).val()
                            });
                        } else if ($(this).hasClass("octal")) {
                            aoData.push({
                                name: "sFilterMin_" + index,
                                value: "o" + $(this).val()
                            });
                        } else {
                            aoData.push({
                                name: "sFilterMin_" + index,
                                value: $(this).val()
                            });
                        }
                        aoData.push({
                            name: "bFilterMinIncl_" + index,
                            value: true
                        });
                    } else if ($(this).hasClass("datatable-column-filter-numeric-max")) {
                        if ($(this).hasClass("hex")) {
                            aoData.push({
                                name: "sFilterMax_" + index,
                                value: "h" + $(this).val()
                            });
                        } else if ($(this).hasClass("octal")) {
                            aoData.push({
                                name: "sFilterMax_" + index,
                                value: "o" + $(this).val()
                            });
                        } else {
                            aoData.push({
                                name: "sFilterMax_" + index,
                                value: $(this).val()
                            });
                        }
                        aoData.push({
                            name: "bFilterMaxIncl_" + index,
                            value: true
                        });
                    } else if ($(this).hasClass("datatable-column-filter-date-min")) {                        
                        aoData.push({
                            name: "sFilterMin_" + index,
                            value: $(this).val()
                        });
                        aoData.push({
                            name: "bFilterMinIncl_" + index,
                            value: true
                        });
                    } else if ($(this).hasClass("datatable-column-filter-date-max")) {
                        aoData.push({
                            name: "sFilterMax_" + index,
                            value: $(this).val()
                        });
                        aoData.push({
                            name: "bFilterMaxIncl_" + index,
                            value: true
                        });
                    } else if ($(this).hasClass("datatable-column-filter-enum")) {
                        aoData.push({
                            name: "sSearch_" + index,
                            value: '*'+$(this).val().replace(/,/g,'*')+'*'
                        });
                    } else if ($(this).hasClass("datatable-column-filter-boolean")) {
                        if($(this).is(":checked")){
                            aoData.push({
                                name: "sSearch_" + index,
                                value: $(this).val()
                            });
                        }
                    } else if ($(this).hasClass("datatable-column-filter-time-min")) {
                        aoData.push({
                            name: "sFilterMin_" + index,
                            value: $(this).val()
                        });
                        aoData.push({
                            name: "bFilterMinIncl_" + index,
                            value: true
                        });
                    } else if ($(this).hasClass("datatable-column-filter-time-max")) {
                        aoData.push({
                            name: "sFilterMax_" + index,
                            value: $(this).val()
                        });
                        aoData.push({
                            name: "bFilterMaxIncl_" + index,
                            value: true
                        });
                    }
                }
            }
        });

        $.post(sSource,
            aoData,
            function(data){
                if (data && data.success === false) {
                    fnCallback({
                        aaData: [],
                        iTotalDisplayRecords: 0,
                        iTotalRecords: 0
                    });
                } else {
                    fnCallback(data);
                }
            },
            "json");
    };
}

function remove_illegal_characters(event){
    var value = $(this).val();
    if (event.data.format == "two_digit_hex") {
        $(this).val(value.substring(0,1).replace(/[^\-0-9A-Fa-f]/g,'')+value.substring(1).replace(/[^0-9A-Fa-f]/g,''));
    } else if (event.data.format == "three_digit_octal") {
        $(this).val(value.substring(0,1).replace(/[^\-0-7]/g,'')+value.substring(1).replace(/[^0-7]/g,''));
    } else if (event.data.type == "numeric") {
        $(this).val(value.substring(0,1).replace(/[^\-0-9]/g,'')+value.substring(1).replace(/[^0-9.]/g,''));
    } else {
        $(this).val(value.substring(0,1).replace(/[^\-0-9]/g,'')+value.substring(1).replace(/[^0-9]/g,''));
    }

}

function enumeration_selection(input, table, column_name, options){

    // Div element id is made unique by associating table id with it.
    var dialog = $("<div/>").attr("id", table.attr("id")+"-"+column_name.replace(" *","").replace(" ","-")+"-enum-selection-dialog").addClass("enum-selection-dialog");
    for(var j = 0; j < options.length; j++){
        dialog
        .append($("<div/>")
            .append($("<label/>")
                .text(options[j])
                .append($("<input/>")
                    .attr({
                        "type":"checkbox",
                        "value":options[j]
                    })
                    ).addClass("datatable-column-filter-logic")
                )
            );
    }
    dialog
    .append($("<div/>")
        .append($("<label/>")
            .text("All")
            .append($("<input/>")
                .attr({
                    "type":"checkbox",
                    "value":"all"
                })
                .bind("click", function(){
                    if($(this).is(":checked")){
                        $("input", dialog).attr("checked", "checked");
                    } else {
                        $("input", dialog).removeAttr("checked");
                    }
                })
                ).addClass("datatable-column-filter-logic")
            )
        );
    dialog.dialog({
        title: column_name+" Options",
        modal:true,
        autoOpen: false,
        width:800,
        buttons: {
            "Ok": function(){
                var filter_string = "";
                $("input:checked", dialog).each(function(){
                    if($(this).val() != "all"){
                        filter_string  += (filter_string?',':'')+$(this).val();
                    }
                });
                input.val(filter_string).attr("title",filter_string);
                $(this).dialog("close");
                table.fnDraw();
            }
        }
    });
    return input.bind("click", function(){
        dialog.dialog("open");
    });
}

function sortObject(o) {
    var sorted = {},
    key, a = [];

    for (key in o) {
        if (o.hasOwnProperty(key)) {
                a.push(key);
        }
    }

    a.sort();

    for (key = 0; key < a.length; key++) {
        sorted[a[key]] = o[a[key]];
    }
    return sorted;
}

// SCJS 0011 START
// Method which aid in quick selection of data based on the logical groups selected from the drop down list
function create_quick_filter(table, logical_group_options, target, recordType, table_headers){

    var column_name = table_headers.eq(target).text();
    var select = $("<select id='"+table.attr("id")+"-quick-filter-select', style='margin-right:20px; width:420px;'/>");
    select.append($("<option/>").val("").text("Displaying All " + recordType));
    var options = logical_group_options;
    options = sortObject(options);
    for(var id in options){
        select.append($("<option/>").val(options[id]).text("Displaying "+ recordType +" in " + options[id]));
    }
    // SCJS QUICK FILTER MODIFIED START
    select.append($("<option/>").val("").text("Custom filter applied").addClass("custom-quick-filter").hide());
    // SCJS QUICK FILTER MODIFIED END
    select
    .bind("change", function(){
        var selectedOption = $("#"+table.attr("id")+"-quick-filter-select").val();
        $("#"+table.attr("id")+"-filter-enum-"+target).val(selectedOption);
        $("input[type='checkbox']", "#"+table.attr("id")+"-"+column_name.replace(" *","").replace(" ","-")+"-enum-selection-dialog").removeAttr("checked");
        $("input[value='"+selectedOption+"']").prop("checked",true);
        table.fnDraw(true);
    })
    table.closest(".dataTables_wrapper")
    .prepend($("<span>* - These fields cannot be edited on this page</span>").addClass("cannot-edit"))
    .prepend(select);
}
// SCJS 0011 END

// QUICK FILTER MODIFIED START
function resetQuickFilter(table, recordType){
    $("#"+table.attr("id")+"-quick-filter-select option")
    .filter(function() {return $.trim( $(this).text() ) == "Displaying All " + recordType;})
    .attr('selected',true);
}

function updateQuickFilter(table){
    $("#"+table.attr("id")+"-quick-filter-select option")
    .filter(function() {return $.trim( $(this).text() ) == "Custom filter applied";})
    .attr('selected',true);
}
// QUICK FILTER MODIFIED END


// SCJS 020 START, CSV download
function generate_download_parameters(filter, datatable){
    var column_filters = $(".datatable-column-filter", filter);

    // get parameters from filters
    var downloadParameters = [];
    column_filters.each(function() {
        var index = $(this).attr("index");

        if ($(this).val() == "" || ($(this).is("[type=radio]") && !$(this).is(":checked")) || index == undefined) {

        } else {

            if ($(this).hasClass("datatable-column-filter-and")) {
                if($(this).is(":checked")){
                    downloadParameters.push({
                        name: "bFilterOr",
                        value: false
                    })
                }
            } else if ($(this).hasClass("datatable-column-filter-or")) {
                if($(this).is(":checked")){
                    downloadParameters.push({
                        name: "bFilterOr",
                        value: true
                    })
                }
            } else {
                downloadParameters.push({
                    name: "bFilterFlag_" + index,
                    value: "true"
                });
                if ($(this).hasClass("datatable-column-filter-string")) {
                    downloadParameters.push({
                        name: "sSearch_" + index,
                        value: $(this).val()
                    })
                } else if ($(this).hasClass("datatable-column-filter-numeric-min")) {
                    downloadParameters.push({
                        name: "sFilterMin_" + index,
                        value: $(this).val()
                    })
                } else if ($(this).hasClass("datatable-column-filter-numeric-max")) {
                    downloadParameters.push({
                        name: "sFilterMax_" + index,
                        value: $(this).val()
                    })
                } else if ($(this).hasClass("datatable-column-filter-date-min")) {
                    downloadParameters.push({
                        name: "sFilterMin_" + index,
                        value: $(this).val()
                    })
                } else if ($(this).hasClass("datatable-column-filter-date-max")) {
                    downloadParameters.push({
                        name: "sFilterMax_" + index,
                        value: $(this).val()
                    })
                } else if ($(this).hasClass("datatable-column-filter-enum")) {
                    downloadParameters.push({
                        name: "sSearch_" + index,
                        value: $(this).val()
                    })
                } else if ($(this).hasClass("datatable-column-filter-boolean")) {
                    if($(this).is(":checked")){
                        downloadParameters.push({
                            name: "sSearch_" + index,
                            value: $(this).val()
                        })
                    }
                }
            }
        }
    });

    // Datatables setting parameters
    var oSettings = datatable.fnSettings();

    var sortIndex = 0;

    $.each(oSettings.aaSorting, function(index, value) {
        downloadParameters.push({
            name: "iSortCol_" + sortIndex,
            value: value[0]
        });
        downloadParameters.push({
            name: "sSortDir_" + sortIndex,
            value: value[1]
        });

        sortIndex++;
    });

    return downloadParameters;
}

function get_file_download(parameters, dialog_panel, ajax_url) {

    // create the form
    var form =
    $("<form/>")
        .attr({
            method: "POST",
            action: ajax_url,
            target: "_self"
        })
        .append(
            // Action
            $("<input/>")
                .attr("type", "hidden")
                .attr("name", "action")
                .attr("value", "csv_download")
        );

    var param_list;
    // Parameters
    for (var i = 0; i < parameters.length; i++)
    {
        param_list += "<input type='hidden' name='" +
            parameters[i].name +
            "' value='" +
            parameters[i].value +"'>";
    }

    form.append(param_list);
    dialog_panel.append(form);

    // Submit the form
    form.submit();

    // Delete the form
    form.remove();
}

function create_csv_download_button(params){
    var filter_div = params.filter_div;
    var datatable = params.datatable;
    var parameters = params.parameters;
    var ajax_url = params.ajax_url;
    var button = $("<button/>");

    return button.text("Download CSV").button({
        icons: { primary: "ui-icon-circle-arrow-s" }
    }).bind({
        "click":function(){
            var downloadParameters = generate_download_parameters(filter_div, datatable);
            if (parameters != null) {
                for (var i = 0; i < parameters.length; i++) {
                    downloadParameters.push({
                        name: parameters[i].name,
                        value: parameters[i].value
                    });
                }
            }
            
            var get_params = "?";
            for(var i in downloadParameters){
                if(get_params.length > 1){
                    get_params += "&"
                }
                get_params += downloadParameters[i].name + "="+downloadParameters[i].value;
            }            
            button.attr("href", ajax_url+get_params+"&action=csv_download");            
            get_file_download(downloadParameters, $("body"), ajax_url);
        }
    });
}

// SCJS 020 END, CSV download

function validate_password(){
    $('#password').keyup(function(){
        $('#result').html(checkStrength($('#password').val()))
    })
}

// check strength of the password
function checkStrength(password){

    //initial strength
    var strength = 0

    //if the password length is less than 6, return message.
    if (password.length < 6) {
        $('#result').removeClass()
        $('#result').addClass('short')
        return 'Too short'
    }

    //length is ok, lets continue.

    //if length is 8 characters or more, increase strength value
    if (password.length > 7) strength += 1

    //if password contains both lower and uppercase characters, increase strength value
    if (password.match(/([a-z].*[A-Z])|([A-Z].*[a-z])/))  strength += 1

    //if it has numbers and characters, increase strength value
    if (password.match(/([a-zA-Z])/) && password.match(/([0-9])/))  strength += 1

    //if it has one special character, increase strength value
    if (password.match(/([!,%,&,@,#,$,^,*,?,_,~])/))  strength += 1

    //if it has two special characters, increase strength value
    if (password.match(/(.*[!,%,&,@,#,$,^,*,?,_,~].*[!,",%,&,@,#,$,^,*,?,_,~])/)) strength += 1

    //now we have calculated strength value, we can return messages

    //if value is less than 2
    if (strength < 2 ) {
        $('#result').removeClass()
        $('#result').addClass('weak')
        return 'Weak'
    } else if (strength == 2 ) {
        $('#result').removeClass()
        $('#result').addClass('good')
        return 'Good'
    } else {
        $('#result').removeClass()
        $('#result').addClass('strong')
        return 'Strong'
    }
}

//display chart
function chart(id) {
    // 3 sec * 60 times = 3 minutes
    var total_retry = 60;   //total retry attempts
    var frequency = 3000;   //check chart status frequency
    var tid;    //setInterval Id
    var i = 0;  //retry count

    //check chart status
    check_status();
    tid = setInterval(function () {
        check_status();
    }, frequency);

    function check_status() {
        $.ajax("ChartImage", {
            type: "POST",
            data: {id: id, action: "status"},
            statusCode: {
                200: function (response) {
                    if (response === "Complete") {
                        $("#loader").hide();
                        display();
                        clearInterval(tid);
                    } else {
                        i++;
                        if (i === total_retry) {
                            clear_and_display_error();
                        }
                    }
                }
            },
            error: function () {
                clear_and_display_error();
            }
        });
    }

    function clear_and_display_error() {
        clearInterval(tid);
        $("#loader").hide();
        alert("Failed to return chart, please try again later");   //display error
    }

    function display() {
        $('#graph-image').attr("src", 'ChartImage?id=' + id + '&action=image').show();
    }
}

function add_logical_groups(afterLabel, data) {
    $("#logical-group-select").empty();
    for (var i = 0; i < data.length; i++) {
        $("#logical-group-select").append($("<div/>")
                .append($("<label/>").text(data[i]))
                .append($("<input type='checkbox' checked='checked'>")
                        .val(data[i])
                        .attr("name", "logical_group_names")
                        .attr("class", "select_one")
                        .addClass("required"))
                );
    }
    if (data.length > 0) {
        if ($('#actionAll').length === 0) {
            $(afterLabel).append($("<div/>")
                    .append($("<input type='checkbox' checked='checked'>")
                            .attr("id", "actionAll"))
                    .append($("<span/>").text("All"))
                    .attr("class", "label_all")

                    );
        } else
            $('#actionAll').attr('checked', 'true');
        $("#actionAll").change(function () {
            $('.select_one').attr('checked', $(this).is(':checked'));
            $(this).removeClass('some_selected');
        });

        $('.select_one').change(function () {
            if ($('.select_one:checked').length == 0) {
                $('#actionAll').removeClass('some_selected').attr('checked', false);
            } else if ($('.select_one:not(:checked)').length == 0) {
                $('#actionAll').removeClass('some_selected').attr('checked', true);
            } else {
                $('#actionAll').addClass('some_selected').attr('checked', false);
            }
        });
    }
    $("#logical-group-select").append($("<hr/>").addClass("hr-clear-float"));
}
