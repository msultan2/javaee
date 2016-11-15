// ADDED IN SCJS 012

function init_logical_group(group_id){
    setHeaderLogicalGroupInfo(group_id);
    createLinks(group_id);
}

function setHeaderLogicalGroupInfo(group_id){
    $("#logical_group").text(group_id);
}

function createLinks(group_id){
 $('#logical-group-users-link')
    .button()
    .bind({
        click:function(){
            var location = "LogicalGroupUsers";
            var id = group_id;
            if(id != undefined){
                location+="?group="+id;
                window.location.href=location;
            }
        }
    });

  $('#logical-group-routes-link')
    .button()
    .bind({
        click:function(){
            var location = "LogicalGroupRoutes";
            var id = group_id;
            if(id != undefined){
                location+="?group="+id;
                window.location.href=location;
            }
        }
    });

  $('#logical-group-spans-link')
    .button()
    .bind({
        click:function(){
            var location = "LogicalGroupSpans";
            var id = group_id;
            if(id != undefined){
                location+="?group="+id;
                window.location.href=location;
            }
        }
    });

   $('#logical-group-detectors-link')
    .button()
    .bind({
        click:function(){
            var location = "LogicalGroupDetectors";
            var id = group_id;
            if(id != undefined){
                location+="?group="+id;
                window.location.href=location;
            }
        }
    });
}
