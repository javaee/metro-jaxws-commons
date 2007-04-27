// define the namespaces
jmaki.namespace("jmaki.widgets.yahoo.calendar");

jmaki.widgets.yahoo.calendar.Widget = function(wargs) {
    var topic = "/yahoo/calendar";
    var self = this;
    var uuid = wargs.uuid;
    this.wrapper = new YAHOO.widget.Calendar(
    "jmaki.attributes.get('" + uuid + "')", uuid);
    
    //read the widget configuration arguments
    if (typeof wargs.args != 'undefined') {
        //overide topic name if needed
        if (typeof wargs.args.topic != 'undefined') {
            topic = wargs.args.topic;
        }  
    }
    
    //default onSelect handler
    var onSelect = function(type,args,obj) {
        var selected = args[0];
        var date = this._toDate(selected[0]);
        jmaki.publish(topic + "/onSelect", {id:uuid,value:date});
    };
    this.wrapper.selectEvent.subscribe(onSelect, this.wrapper, true);
    
    //read date from value
    if (typeof wargs.value != 'undefined') {
        var date = new Date(wargs.value);
        this.wrapper.select(date);
    }
    
    
    /**
     * Returns first selected date
     */
    this.getValue = function() {
        if (this.wrapper.getSelectedDates().length >0) {
            return this.wrapper.getSelectedDates()[0];
        } else {
            return null;
        }
    }
    
    // add a saveState function
    if ( wargs.service) {
        this.saveState = function() {
            if (self.getValue() == null) return;
            // we need to be able to adjust this
            var url = wargs.service;
            var _val =  self.getValue().toString();
            url = url + "?cmd=update";
            jmaki.doAjax({url: url, method: "post", content: {value : _val}, callback: function(req) {
                if (req.readyState == 4) {
                    if (req.status == 200) {
                        // take some action if needed
                    }
                }
            }});
        }
    }
    this.wrapper.render();
}