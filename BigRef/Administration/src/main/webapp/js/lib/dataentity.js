var DataEntity = function DataEntity(name) {
    this.name = name;
    this.fetched_id = null;
    this.hidden = [];
    this.noneditables = [];
    this.viewModel = {};
    this.data = {};
    this.old_data = {};
};

DataEntity.columns = {};
DataEntity.mandatory = {};
DataEntity.options = {};
DataEntity.prototype.hide = function hide(fields) {
    this.hidden = fields;
    return this;
};


DataEntity.prototype.load = function load(id) {
    this.fetched_id = id;
    this.fetchExistingData(id);
    return this;
};
DataEntity.prototype.use = function use(resource, el) {
    this.create(resource);
    var a = el.wrap($("<div />").attr("data-bind", "stopBinding: true"));
    this.el = a;
    return this;
};
DataEntity.prototype.create = function create(resource) {
    var me = this;
    me.el = $("<div />").attr("id", this.name + "-dialog")
            .appendTo($("#dialog-container"));
    me.resource = resource;
    me.onChange = function() {
    };
    me.el.wrap($("<div />").attr("data-bind", "stopBinding: true"));
    if (!_.has(DataEntity.columns, this.name)) {
        $.ajax({
            dataType: "json",
            cache: true,
            url: me.resource + "/types",
            success: function(columns) {

                DataEntity.columns[me.name] = _.map(columns, function(c) {
                    c.hide = _.indexOf(me.hidden, c.propertyName) != -1;
                    return c;
                });
                me.columns = DataEntity.columns[me.name];
                me.pk = _.filter(me.columns, function(ele, ind, arr) {
                    return ele.primaryKey;
                })[0].propertyName;

            },
            async: false
        });
    } else {
        me.columns = DataEntity.columns[me.name];
    }

    return this;
};

DataEntity.prototype.alone = function() {
    this.el.append($("<table />").addClass("splitbox")
            .append($("<tbody />").append($("<tr />"))));
    this.fields("table");
    return this;
};


DataEntity.prototype.resource = function(resource) {
    var me = this;
    me.resource = resource;
    if (!_.has(DataEntity.columns, this.name)) {
        $.ajax({
            dataType: "json",
            cache: true,
            url: me.resource + "/types",
            success: function(columns) {
                DataEntity.columns[me.name] = _.map(columns, function(c) {
                    c.hide = _.indexOf(me.hidden, c.propertyName) != -1;
                    return c;
                });
                me.columns = DataEntity.columns[me.name];
                me.pk = _.filter(me.columns, function(ele, ind, arr) {
                    return ele.primaryKey;
                })[0].propertyName;
            },
            async: false
        });
    }
    return this;
};


DataEntity.prototype.forDTH = function forDTH(dth) {
    var me = this;
    this.columns = _.map(dth.columns, function(c) {
        c.hide = _.indexOf(me.hidden, c.propertyName) != -1;
        return c;
    });
    this.pk = dth.pk;
    this.name = "datatable";
    this.resource = dth.resource;
    this.el = $(dth.updid).wrap($("<div />").attr("data-bind", "stopBinding: true"));
    this.onChange = function() {
        return dth.datatable.fnDraw();
    };
    this.alone();
    return this;
};


DataEntity.prototype.extend = function extend(name, f) {
    var me = this;
    DataEntity.prototype[name] = f;
};


DataEntity.prototype.addNet = function addNet(cb) {
    var me = this;
    return function() {
        return $.ajax({
            type: "POST",
            url: me.resource + "/add",
            data: JSON.stringify(me.getData()),
            dataType: "json",
            async: false,
            contentType: "application/json; charset=utf-8"
        }).done(function(data) {
            for (var key in data) {
                me.viewModel[table(key)][field(key)](data[key]);
                if (key == me.pk) {
                    me.fetched_id = data[key];
                }
            }
            if (cb) {
                cb(data);
            }
        });
    };
};

DataEntity.prototype.save = function save() {
    var res;
    if (this.fetched_id) {
        this.updateNet(this.fetched_id, null)();
        return this.fetched_id;
    } else {
        this.addNet(function(data) {
            res = data;
        })();
    }
    for (var key in this.old_data) {
        this.old_data[key] = this.viewModel[table(key)][field(key)]();
    }
    return _.chain(res).values().first().value();
};

DataEntity.prototype.updateNet = function updateNet(id, cb) {
    var me = this;
    return function() {
        return $.ajax({
            type: "POST",
            url: me.resource + "/" + id + "/update",
            data: JSON.stringify(me.getChangedData()),
            dataType: "json",
            contentType: "application/json; charset=utf-8",
            async: false
        }).done(cb);
    };
};

DataEntity.prototype.add = function add(cb) {
    cleanupDialog();
    ko.applyBindings(this.viewModel, this.el[0]);
    this.showDialog(this.addNet(cb), "add");
};

DataEntity.prototype.update = function update(id, cb) {
    cleanupDialog();
    this.fetchExistingData(id);
    ko.applyBindings(this.viewModel, this.el[0]);
    this.showDialog(this.updateNet(id, cb), "update");
};
DataEntity.prototype.populateOptions = function populateOptions(element, resource) {
    function success(data) {
        for (var i in data) {
            element.append($("<option />")
                    .attr("value", data[i].value).text(data[i].label));
        }
    }
    if (!_.has(DataEntity.options, resource)) {
        $.ajax({
            dataType: "json",
            url: resource,
            cache: true,
            data: {},
            success: function(data){
                DataEntity.options[resource] = data;
                success(data);
            },
            async: false
        });
    } else {
        success(DataEntity.options[resource]);
    }
};
DataEntity.prototype.fetchExistingData = function fetchExistingData(id) {
    var me = this;
    $.ajax({
        dataType: "json",
        url: me.resource + "/" + id,
        async: false,
        success: function(result) {
            for (var i in me.columns) {
                var cname = me.columns[i].propertyName;
                var res = _.filter(me.noneditables, function(a) {
                    return a.name == cname;
                });
                if (res.length == 1) {
                    res[0].obj.attr("disabled", "disabled");
                }
                if (table(cname) == table(me.pk)) {
                    me.old_data[cname] = result[cname];
                    me.set(cname, result[cname]);
                } else {
                    if (me.viewModel.hasOwnProperty(table(cname))) {
                        me.viewModel[table(cname)][field(cname)](result[cname]);
                    }
                }
            }
            //  initTimedates();
        }
    });
};



DataEntity.prototype.knock = function knock(type, s, val) {
    s = (s + "").split(".");
    var kval = ko.observable(val);
    var msg = "Format error";
    if (type == "INTEGER") {
        kval.extend({pattern: {message: msg, params: "^[0-9]+$"}});
    } else if (type == "DOUBLE") { // make this right
        kval.extend({pattern: {message: msg, params: "^[0-9.]+$"}});
    }
    if (s.length == 2) {
        var _table = s[0], _field = s[1];
        if (this.viewModel.hasOwnProperty(_table)) {
            this.viewModel[_table][_field] = kval;
        } else {
            this.viewModel[_table] = {};
            this.viewModel[_table][_field] = kval;
        }
    } else {
        this.viewModel[_table] = kval;
    }
};

DataEntity.prototype.fields = function fields(target) {
    // target == {table,form}
    // number of rows

    var me = this;

    var wrap = $("<div />").addClass("centre");
    var span = $("<span />").addClass("error centre");
    wrap.append(span);
    me.el.append(wrap);

    if (target == null) {
        for (var i in this.columns) {
            this.knock(this.columns[i].filterType, this.columns[i].propertyName, null);
        }
        ko.applyBindings(this.viewModel, this.el[0]);
        return this;
    }

    var inserts = {form: function insert(label, input) {
            var n = 5;
            if (typeof this.counter == "undefined") {
                this.counter = 0;
            }
            if (this.counter % n == 0) {
                this.fs = $("<fieldset />").appendTo(me.el);
            }

            this.fs.append(input);
            this.fs.append(label);
            this.counter++;
        }, table: function(label, input) {
            me.el.find("tbody")
                    .append($("<tr />")
                    .append($("<td />").addClass("left").html(label))
                    .append($("<td />").addClass("right").html(input))
                    );
        }};

    var insert = inserts[target];

    var pre = "data_" + this.name + "_";
    var selectables = {};
    var curfs = $("<fieldset />");
    for (var i in this.columns) {

        if (this.columns[i].viewOnly) {
            continue;
        }
        var label = $("<label />").attr("for", did);
        var field = $("<div />");
        if (this.columns[i].hide) {

            label.css("display", "none");
            field.css("display", "none");
        }
        var selects = selectables.hasOwnProperty(
                this.columns[i].propertyName) || this.columns[i].selectableId;
        if (selects) {
            if (this.columns[i].selectableId) {
                selectables[this.columns[i].selectableLabel] =
                        {
                            column: this.columns[i],
                            id: this.columns[i].propertyName,
                            resource: this.columns[i].selectableResource
                        };
            }
            if (selectables.hasOwnProperty(this.columns[i].propertyName)) {
                var myid = selectables[this.columns[i].propertyName].id;
                var did = pre + myid;
                label.text(this.columns[i].colName);

                var select = $("<select />").attr("id", did);
                select.attr("data-bind", "value: " + myid);
                select.attr("name", myid);

                this.populateOptions(select, selectables[this.columns[i].propertyName].resource);

                if (!selectables[this.columns[i].propertyName].column.editable) {
                    this.noneditables.push({name: this.columns[i].propertyName, obj: select});
                }
                this.knock("STRING", myid, select.children().first().val());

                if (!this.columns[i].hide) {
                    insert(label, select);
                }
            }
        } else {
            var input = $("<input />");

            input.attr("data-bind", "value: " + this.columns[i].propertyName);

            this.knock(this.columns[i].filterType, this.columns[i].propertyName, null);


            if (this.columns[i].primaryKey) {
                input.attr("disabled", "disabled");
            }

            if (!this.columns[i].editable) {
                this.noneditables.push({name: this.columns[i].propertyName, obj: input});
            }

            if (this.columns[i].filterType == "STRING") {
                input.attr("type", "text").attr("name", this.columns[i].propertyName)
                        .attr("id", pre + this.columns[i].propertyName)
                        .addClass("centre");
            } else if (this.columns[i].filterType == "INTEGER") {
                input.attr("type", "text").addClass("integer centre")
                        .attr("name", this.columns[i].propertyName)
                        .attr("id", pre + this.columns[i].propertyName)
                        .keyup(function() {
                    this.value = this.value.replace(/[^\d\-]/g, '');
                });
            } else if (this.columns[i].filterType == "DOUBLE") {
                input.attr("type", "text").addClass("double centre")
                        .attr("name", this.columns[i].propertyName)
                        .attr("id", pre + this.columns[i].propertyName)
                        .keyup(function() {
                    this.value = this.value.replace(/[^0-9\.\-]/g, '');
                });
            } else if (this.columns[i].filterType == "DATE") {
                input.attr("type", "text").addClass("date centre")
                        .attr("name", this.columns[i].propertyName)
                        .attr("readonly", "readonly")
                        .attr("id", pre + this.columns[i].propertyName);
            } else if (this.columns[i].filterType == "BOOLEAN") {
                input.attr("type", "checkbox").addClass("boolean");
                input.attr("data-bind", "checked: " + this.columns[i].propertyName);
            }
            if (this.columns[i].hide) {
                input.css("display", "none");
            }
            input.attr("name", this.columns[i].propertyName)
                    .attr("id", pre + this.columns[i].propertyName);
            label.text(this.columns[i].colName).attr("for", this.columns[i].propertyName);
            if (!this.columns[i].hide) {
                insert(label, input);
            }
            input = $("<input />");
        }
        // append * to mandatory fields
        var mand = me.getMandatoryFields();
        _.each(_.filter(me.columns, function(c) {
            return c.visible && c.editable;
        }), function(c) {
            _.each(mand, function(m) {
                if (c.propertyName === m) {
                    $("label[for='" + m + "']").text(function(index, value) {
                        if (_.indexOf(value, "*") != -1) {
                        } else {
                            return value = value + " *";
                        }
                    })
                }
            })
        })
    }

    if (_.keys(this.viewModel).length == 1) {
        ko.applyBindings(this.viewModel, this.el[0]);
    }
    return this;
};

function IsEmail(email) {
    var regex = /^([a-zA-Z0-9_.+-])+\@(([a-zA-Z0-9-])+\.)+([a-zA-Z0-9]{2,4})+$/;
    return regex.test(email);
}

DataEntity.prototype.get = function(c) {
    return this.viewModel[c.split(".")[0]][c.split(".")[1]]();
};

DataEntity.prototype.set = function(c, val) {
    var el = $("#data_" + this.name + "_" + escapeDot(c));
    if (el.is("select") && el.find("option[value='" + val + "']").length == 0) {
        el.append($("<option />").attr("value", val));
    }

    var f = this.viewModel[c.split(".")[0]][c.split(".")[1]];
    if (f) {
        f(val);
    }
};


function field(s) {
    if (!s) {
        return null;
    }
    var ss = s.split(".");
    if (ss.length == 2) {
        return ss[1];
    } else {
        return s;
    }
}

function table(s) {
    if (!s) {
        return null;
    }
    var ss = s.split(".");
    if (ss.length == 2) {
        return ss[0];
    } else {
        return s;
    }
}


DataEntity.prototype.getData = function() {
    var data_ = {};

    for (var i in this.columns) {
        if (this.columns[i].primaryKey || this.columns[i].viewOnly ||
                table(this.columns[i].propertyName) != table(this.pk) ||
                !this.get(this.columns[i].propertyName)) {
            continue;
        }
        data_[this.columns[i].propertyName] = {"new": this.get(this.columns[i].propertyName)};
    }
    return data_;
};

DataEntity.prototype.getChangedData = function() {
    var newdata = {};

    for (var key in this.old_data) {
        var old = this.old_data[key];
        var s = key.split(".");
        var col = _.filter(this.columns, function(c) {
            return c.propertyName == key;
        })[0];
        var val = this.viewModel[s[0]][s[1]]();
        if (val && _.indexOf(this.pk, ".") == -1 || table(key) ==
                table(this.pk) && !col.viewOnly &&
                !col.primaryKey && old != val) {

            newdata[key] = {"new": val,
                "old": old};
        }
    }
    return newdata;
};

DataEntity.prototype.getMandatoryFields = function() {
    // GET /api/meta/mandatory/columns/<tablename>
    var me = this;
    var tablename = me.pk.split(".")[0];
    if (!_.has(DataEntity.mandatory, me.name)) {
        $.ajax({
            dataType: "json",
            cache: true,
            url: "/api/meta/mandatory/columns/" + tablename,
            success: function(columns) {
                DataEntity.mandatory[me.name] = _.map(_.filter(columns, function(m) {
                    if (m == "id") {
                    } else {
                        return m;
                    }
                }), function(c) {
                    return tablename + "." + c;
                });
            },
            async: false
        });
    }
    return  DataEntity.mandatory[me.name];
}

DataEntity.prototype.getUniqueFields = function() {
    // GET /api/meta/unique/columns/<tablename>
    var me = this;
    var tablename = me.pk.split(".")[0];
    var tmp;
    $.ajax({
        dataType: "json",
        url: "/api/meta/unique/columns/" + tablename,
        success: function(columns) {
            tmp = _.map(_.filter(columns, function(m) {
                if (m == "id") {
                } else {
                    return m;
                }
            }), function(c) {
                return tablename + "." + c;
            });
        },
        async: false
    });
    return tmp;
}

DataEntity.prototype.check = function(column, value, type) {
    // POST /api/meta/unique/check/<tablename> {column:"",value:""} - returns {result: true/false}
    var me = this;
    var tablename = me.pk.split(".")[0];
    var uniqueConstraint;
    var data = {column: column, value: value, type: type};
    $.ajax({
        type: "POST",
        dataType: "json",
        url: "/api/meta/unique/check/" + tablename,
        data: JSON.stringify(data),
        success: function(success) {
            uniqueConstraint = success["result"];
        },
        contentType: "application/json; charset=utf-8",
        async: false
    });
    return uniqueConstraint;
}

DataEntity.prototype.updateUser = function() {
    var me = this;
    var cdata = me.getChangedData();
    _.chain(me.columns).filter(function(c) {
        return _.contains(me.getUniqueFields(), c.propertyName) && _.has(cdata, c.propertyName);
    }).each(function(c) {
        if (!me.check(field(c.propertyName), me.get(c.propertyName), c.filterType)) {
            alert(field(c.propertyName) + " '" + me.get(c.propertyName) + "' already exits");
        }
    });
}

DataEntity.prototype.checkMandatory = function() {
    var me = this;
    var mdata = me.getMandatoryFields()
    _.each(_.filter(me.columns, function(c) {
        return c.visible && c.editable;
    }), function(c) {
        _.each(mdata, function(m) {
            if (c.propertyName === m) {
                $("[name='" + m + "']").each(function() {
                    if (this.value.replace(/ /gi, "") === "") {
                        $(this).css("border-color", "red");
                    } else {
                        $(this).css("border-color", "#eee");
                    }
                });
            }
        });
    });
}

DataEntity.prototype.showDialog = function(action, actiontype) {
    var me = this;

    $(".splitbox input[type=text]:not(.date)").first().focus();
    initTimedates();
    this.el.dialog({
        modal: true,
        width: 600,
        height: 400,
        title: me.name + " - Add/update entry",
        buttons: {
            Cancel: function() {
                $(this).dialog("close");
            },
            Save: function() {
                // mandatory field validation
                var mdata = me.getMandatoryFields();
                var merror = false;
                _.each(_.filter(me.columns, function(c) {
                    return c.visible && c.editable &&
                            me.pk != c.propertyName;
                }), function(c) {
                    if (_.contains(mdata, c.propertyName)) {
                        $("[name='" + c.propertyName + "']").each(function() {
                            if (this.value.replace(/ /gi, "") === "") {
                                $(this).css("border-color", "red");
                                merror = true;
                            } else {
                                $(this).css("border-color", "#eee");
                            }
                        });
                    }
                });
                // end of mandatory field validation

                // unique field validation
                if (actiontype === "add") {
                    var udata = me.getUniqueFields();
                    _.chain(me.columns)
                            .filter(function(c) {
                        return _.contains(udata, c.propertyName) && c.visible && c.editable && c.propertyName != me.pk;
                    }).each(function(c) {
                        console.log(me.get(c.propertyName));
                        if (me.get(c.propertyName) !== null && !me.check(field(c.propertyName), me.get(c.propertyName), c.filterType) &&
                                me.get(c.propertyName) !== "") {
                            $(".error").text(field(c.propertyName) + " '" + me.get(c.propertyName) + "' already exits");
                        } else {
                            cleanupDialog();
                        }
                    });
                } else if (actiontype === "update") {
                    var udata = me.getUniqueFields();
                    var cdata = me.getChangedData();
                    _.chain(me.columns)
                            .filter(function(c) {
                        return _.contains(udata, c.propertyName) && _.has(cdata, c.propertyName) &&
                                c.propertyName != me.pk;
                    }).each(function(c) {

                        if (!me.check(field(c.propertyName), me.get(c.propertyName), c.filterType)) {
                            $(".error").text(field(c.propertyName) + " '" + me.get(c.propertyName) + "' already exits");
                        } else {
                            cleanupDialog();
                        }
                    });
                }
                // end of unique field validation

                // If valid entry then insert 
                if (!merror && $(".error").text() == "") {
                    action().error(function(err) {
                        console.log("ERROR");
                    }).success(function() {
                        cleanupDialog();
                        me.onChange();
                    });
                    $(this).dialog("close");
                }
            }
        }, close: function(e, ui) {
            me.el.find("input,select").each(function(i, v) {
                if ($(v).attr("type") != "radio") {
                    $(v).val("");
                }
                $(v).removeAttr("disabled");
            });
        }
    });
};

function cleanupDialog() {
    $(".error").text("");
}