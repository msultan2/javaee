//(function() {




var test;
function init_detector_manage(detector_id,detector_name) {
    $("#detector_name").text(detector_name);
    var CommandBuilder = function() {
        this.commands = ko.observableArray();
    };
    var cb = new CommandBuilder();

    CommandBuilder.prototype.add = function(cmd, arg) {
        if (!_.findWhere(this.commands(), {cmd: cmd})) {
            this.commands.push({"cmd": cmd, "arg": arg, to_s: function() {
                    return cmd + (arg ? ":" + arg : "");
                }});
        }
    };

    CommandBuilder.prototype.join = function() {
        return _.map(this.commands(), function(c) {
            return c.to_s();
        }).join(",");
    };

    CommandBuilder.prototype.clear = function() {
        this.commands([]);
    };
    CommandBuilder.prototype.empty = function() {
        return this.commands().length == 0;
    };
    function generateSeed(detector_id) {
        $.post("/DetectorManager?action=new-seed&id=" + detector_id, function(source) {
            if (source !== undefined) {
                var rc = jQuery.parseJSON(source);
                if (rc.success) {
                    $.notify("A new seed has been generated and uploaded to the outstation. Now awaiting confirmation.", "success");
                } else {
                    $.notify("Failed to send seed file", "error");
                }
            }
        });
    }
    function reloadKeys(detector_id) {
        $.post("/DetectorManager?action=reload-key&id=" + detector_id, function(source) {
            if (source !== undefined) {
                var rc = jQuery.parseJSON(source);
                if (rc.success) {
                    $.notify("A new key has been generated and uploaded to the outstation.", "success");
                } else {
                    $.notify("Connection not established", "error");
                }
            }
        });
    }
    function addOutstationToInstationKey(detector_id) {
        $.post("/DetectorManager?action=add-outstation-to-instation-key&id=" + detector_id, {});
    }
    function removeOutstationToInstationKey(detector_id) {
        $.post("/DetectorManager?action=remove-outstation-to-instation-key&id=" + detector_id, {});
    }
    function getStatus(detector_id) {
        var d;
        $.getJSON("/DetectorManager?action=get-status&id=" + detector_id, function(data) {
            d = data;
        });
        return d;
    }

    $.ajaxSetup({async: false});

    ko.applyBindings({commands: cb.commands});
    var status = getStatus(detector_id);
    if (!status) {
        $.notify("Not logged in.","error");
    }
    
    $("input[name=cseed]").val(status.seed);

    $("#seed_change").click(function() {
        cb.add("changeSeed");
    });

    $("#seed_gen").click(function() {
        generateSeed(detector_id);
        
    });
    $("#key_change").click(function() {
        reloadKeys(detector_id);
    });
    $("#add_generic_key").click(function() {
        addOutstationToInstationKey(detector_id);
        $.notify("Generic Outstation to Instation key added to Instation authorized file.", "success");
    });
    $("#remove_generic_key").click(function() {
        removeOutstationToInstationKey(detector_id);
        $.notify("Generic Outstation to Instation key removed from Instation authorized file.", "success");
    });
    $("#ssh_open").click(function() {
        var arg = $("input[name=sshopen]");
        if (isFinite(arg.val()) && arg.val() >= 1 && arg.val() <= 65535 && arg.val().match(/^\d+$/) && arg.val() != 50000) {
            cb.add("openSSHConnection", parseInt(arg.val()));
        } else if (arg.val() == 50000) {
            $.notify("Cannot establish SSH connection on port 50000, please use other port");
            arg.val("");
        } else {
            $.notify("SSH port must be an integer between 1 and 65535","error");
            arg.val("");
        }
    });
    $("#ssh_close").click(function() {
        cb.add("closeSSHConnection");
    });
    $("#get_status").click(function() {
        cb.add("getStatusReport");
    });
    $("#reboot").click(function() {
        cb.add("reboot");
    });
    $("#reload").click(function() {
        cb.add("reloadConfiguration");
    });

    $("#flush").click(function() {
        cb.add("flushBackground");
    });

    $("#latch").click(function() {
        cb.add("latchBackground");
    });

    $("#cmds_send").click(function() {
        if (!cb.empty()) {
            $.post("/DetectorManager?action=queue-commands&id=" + detector_id + "&commands=" + cb.join(), {});
            $.notify("The commands will be sent to the outstation as a response to the next request. This might take some time.", "success");
            cb.clear();
        } else {
            $.notify("No commands to enqueue", "error");
        }
    });
    $("#cmds_clear").click(function() {
        cb.clear();
    });
}


//})();