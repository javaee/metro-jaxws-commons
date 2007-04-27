// define the namespaces
jmaki.namespace("jmaki.widgets.yahoo.tabbedview");

jmaki.widgets.yahoo.tabbedview.Widget = function(wargs) {
    
    var self = this;
    
    var topic = "/yahoo/tabbedview";
    var tabs = [];
    var selected = 0;
    
    self._tabView = new YAHOO.widget.TabView(wargs.uuid);
    
    if (wargs.selected) {
        selected = wargs.selected;
    }
    for(var _ii=0; _ii < wargs.value.tabs.length; _ii++) {
        var _row = wargs.value.tabs[_ii];
        if (!_row.url) {
            var _tid = wargs.uuid + '_tab_' + _ii;
            var h =  document.getElementById(wargs.uuid).parentNode.clientHeight - 38; // TODO : Get the true label height
            var w =  document.getElementById(wargs.uuid).parentNode.clientWidth - 2;
            var content = ("<div style='height:" + h + "px;width:" + w + "' id='" + _tid +"'>" + _row.content + "</div>");           
            var _r = new YAHOO.widget.Tab({
                label: _row.label,
                content: content,
                active: (_ii == selected)
            });
            self._tabView.addTab(_r);
        } else {
            var _tid = wargs.uuid + '_tab_' + _ii;
            // calculate height here
            var h =  document.getElementById(wargs.uuid).parentNode.clientHeight - 35; // TODO : Get the true label height
            var w =  document.getElementById(wargs.uuid).parentNode.clientWidth - 2;
            if (h <= 50) h = 300;
            var _r = new YAHOO.widget.Tab({
                label: _row.label,
                active: (_ii == selected)
            });                
            self._tabView.addTab(_r);                    
        
            var of = _row.overflow;
            if (typeof of == 'undefined') of = false;
            var iframe = _row.iframe;
            if (typeof iframe == 'undefined') of = false;                  
            var cv = _r.get('contentEl');

            cv.id = _tid;         
            var iargs = {
                target: cv,
                useIframe : iframe,
                overflow: of,
                topic : _row.topic,
                url : _row.url,
                startHeight : h,
                startWidth : w,
                autosize : true
            };           
            var dcontainer = new jmaki.DContainer(iargs);          
        }
    }
}