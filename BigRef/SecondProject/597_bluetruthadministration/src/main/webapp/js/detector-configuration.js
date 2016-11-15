var detector_configuration_validator;


function init_detector_configuration(detectorId){
    $(".detector-configuration-edit-buttons").buttonset();
    
    $(".save-button").bind({
        "click":function(){
            save_configuration();
        }
    });
    
    $.post("DetectorConfigurationManager",
    {
        "action":"get",
        "detector_id":detectorId
    },
    function(data){
        if(data.success){
            var detector_data = data.data[0];
            $("#detector-name-crumb").text(detector_data.detector_name.value);
            $("#detector-id-crumb").text(detector_data.detector_id.value);

            var configuration_settings = $("<div class=\"configuration-group ui-corner-all information-panel\" >").append($("<h1/>").text("Configuration Settings"));
                
            for(var key in detector_data){
                if(key != "detector_name" && 
                    key != "detector_id" ){

                    var label = key.replace(/([A-Z0-9])/g, ' $1').replace(/^./, function(str){
                        return str.toUpperCase();
                    });

                    if(detector_data[key].type == "Time"){
                        configuration_settings.append(create_configuration_div_timepicker(key,label,detector_data[key].value));
                    } else if(detector_data[key].type == "Timestamp"){
                        configuration_settings.append(create_configuration_div_datetimepicker(key,label,detector_data[key].value));
                    } else if(detector_data[key].type == "Boolean"){
                        configuration_settings.append(create_configuration_div_boolean(key,label,detector_data[key].value));
                    } else if(detector_data[key].type == "Integer"){
                        configuration_settings.append(create_configuration_div_integer(key,label,detector_data[key].value));
                    } else if(detector_data[key].type == "Double"){
                        configuration_settings.append(create_configuration_div_double(key,label,detector_data[key].value));
                    } else if(key == "daysToIgnore"){
                        configuration_settings.append(create_configuration_div_days_to_ignore(key,label,detector_data[key].value));
                    } else if(key == "urlIniFileDownload"){
                        configuration_settings.append(create_configuration_div_ini_file(key,label,detector_data[key].value,detectorId));
                    } else {
                        configuration_settings.append(create_configuration_div(key,label,detector_data[key].value));
                    }



                }
            }

            configuration_settings.append($("<input/>")
                .attr("name", "detector_id")
                .attr("id", "detector_id")
                .attr("type", "hidden")
                .val(detector_data.detector_id.value))
            .append($("<input/>")
                .attr("name", "action")
                .attr("id", "action")
                .attr("type", "hidden")
                .val("update"));

            detector_configuration_validator = $("#configuration-form").append(configuration_settings).validate();

            $(".timepicker").timepicker({
                timeFormat: 'hh:mm:ss',
                showSecond: true
            }).attr("readonly","readonly");
            $(".datepicker").datepicker({
                dateFormat: 'yy-mm-dd'
            }).attr("readonly","readonly");
            $(".datetimepicker").datetimepicker({
                dateFormat: 'yy-mm-dd',
                timeFormat: 'hh:mm:ss.0',
                showSecond: true
            }).attr("readonly","readonly");
        }
    },
    "json");
}

function save_configuration(){
    var form = $("#configuration-form");

    if(form.valid()){
        $.post("DetectorConfigurationManager",
            $("#configuration-form").serialize(),
            function(data){
                if(data.success){
                    display_information_dialog("Detector Configuration Update Successful", "The detector configuration was successfully updated.");
                } else {
                    display_information_dialog("Detector Configuration Update Unsuccessful", data.message);
                }
            },
            "json");
    } else {
        var numberOfInvalids = detector_configuration_validator.numberOfInvalids();
        var message = "Configuration cannot be updated. "+numberOfInvalids+" invalid field";
        if(numberOfInvalids > 1){
            message += "s";
        }
        message += " specified.";
        display_information_dialog("Configuration Invalid", message);
    }
}

function create_configuration_div(id, title, value){
    if(value == null){
        value = "";
    }
    return $("<div class=\"configuration\"><label>"+title+":</label><input id=\""+id+"\" name=\""+id+"\" type=\"text\" value=\""+value+"\"/></div>");
}

function create_configuration_div_ini_file(id, title, value, detectorId){
    if(value == null){
        value = "";
    }

    var config_row = $("<div class=\"configuration\"/>");
    var input = $("<input id=\""+id+"\" name=\""+id+"\" type=\"text\" value=\""+value+"\"/>");
    var download_link = $("<a href=\""+value + detectorId +"_ini.txt\">Download</a>");
    config_row
    .append($("<label>"+title+":</label>"))
    .append(input.change(function(){
        download_link.attr("href", $(this).val() + detectorId +"_ini.txt");
    }))
    .append(download_link);

    return config_row;
}

function create_configuration_div_integer(id, title, value){
    if(value == null){
        value = "";
    }
    return $("<div class=\"configuration\"><label>"+title+":</label><input id=\""+id+"\" name=\""+id+"\" type=\"text\" class=\"digits\" value=\""+value+"\"/></div>");
}

function create_configuration_div_double(id, title, value){
    if(value == null){
        value = "";
    }
    return $("<div class=\"configuration\"><label>"+title+":</label><input id=\""+id+"\" name=\""+id+"\" type=\"text\" class=\"number\" value=\""+value+"\"/></div>");
}

function create_configuration_div_boolean(id, title, value){
    if(value == null){
        value = "";
    }

    var options;
    if(value){
        options = "<option selected=\"selected\" value=\"true\">TRUE</option><option value=\"false\">FALSE</option>";
    } else {
        options = "<option value=\"true\">TRUE</option><option selected=\"selected\" value=\"false\">FALSE</option>";
    }

    return $("<div class=\"configuration\"><label>"+title+":</label><select id=\""+id+"\" name=\""+id+"\">"+
        options+
        "</select></div>");
}

function create_configuration_div_timepicker(id, title, value){
    if(value == null){
        value = "";
    }
    return $("<div class=\"configuration\"><label>"+title+":</label><input id=\""+id+"\" name=\""+id+"\" type=\"text\" value=\""+value+"\" class=\"timepicker\"/></div>");
}

function create_configuration_div_datetimepicker(id, title, value){
    if(value == null){
        value = "";
    }
    return $("<div class=\"configuration\"><label>"+title+":</label><input id=\""+id+"\" name=\""+id+"\" type=\"text\" value=\""+value+"\" class=\"datetimepicker\"/></div>");
}

function create_configuration_div_days_to_ignore(id, title, value){
    var value_array = value.split(";");
    var configuration_row_date = $("<div class=\"configuration\"/>");
    var configuration_row_days = $("<div class=\"configuration\"/>");
    var actual_input = $("<input/>").attr("type","hidden").attr("name", id).attr("id", id).val(value);

    var date_one;
    var date_two;
    var day_one;
    var day_two;
    var day_three;
    var day_four;

    if(value_array.length < 6){
        date_one = $("<input/>").addClass("datepicker");
        date_two = $("<input/>").addClass("datepicker");
        day_one = create_weekday_select(0);
        day_two = create_weekday_select(0);
        day_three = create_weekday_select(0);
        day_four = create_weekday_select(0);
    } else {
        date_one = $("<input/>").addClass("datepicker").val(value_array[0]);
        date_two = $("<input/>").addClass("datepicker").val(value_array[1]);
        day_one = create_weekday_select(value_array[2]);
        day_two = create_weekday_select(value_array[3]);
        day_three = create_weekday_select(value_array[4]);
        day_four = create_weekday_select(value_array[5]);
    }

    var update_callback = function(){
        actual_input.val(date_one.val()+";"+date_two.val()+";"+day_one.val()+";"+day_two.val()+";"+day_three.val()+";"+day_four.val());
    };

    configuration_row_date
    .append($("<label/>").text(title+" (Date):"))
    .append(date_one.change(update_callback))
    .append(date_two.change(update_callback));

    configuration_row_days
    .append($("<label/>").text(title+" (Day):"))
    .append(day_one.change(update_callback))
    .append(day_two.change(update_callback))
    .append(day_three.change(update_callback))
    .append(day_four.change(update_callback));

    update_callback();

    return $("<span/>").append(configuration_row_date).append(configuration_row_days).append(actual_input);

}

function create_weekday_select(default_value){
    var select = $("<select/>")
    .append($("<option/>").text("None").val(0))
    .append($("<option/>").text("Monday").val(1))
    .append($("<option/>").text("Tuesday").val(2))
    .append($("<option/>").text("Wednesday").val(3))
    .append($("<option/>").text("Thursday").val(4))
    .append($("<option/>").text("Friday").val(5))
    .append($("<option/>").text("Saturday").val(6))
    .append($("<option/>").text("Sunday").val(7));
    select.find("option[value='"+default_value+"']").attr("selected","selected");
    return select;
}