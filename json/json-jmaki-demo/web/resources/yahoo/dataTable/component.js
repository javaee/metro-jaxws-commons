// define the namespaces
jmaki.namespace("jmaki.widgets.yahoo.dataTable");

jmaki.widgets.yahoo.dataTable.Widget = function(wargs) {
    
    var container = document.getElementById(wargs.uuid);
    var filter = "jmaki.filters.tableFilter";
    var self = this;
    var _model;
    var cm;
 
    var data;
    // yahoo normalized data
    var nData =[];
    var cols = [];
    var cold;
    if (wargs.args && wargs.args.columns) {
        cold =   wargs.args.columns;
    }
    if (wargs.args && wargs.args.filter) {
        filter = wargs.args.filter;
    }
    var schema = [];

    
    if (wargs.value) {
        // convert value if a jmakiRSS type
        if (wargs.value.dataType == 'jmakiRSS') {
           wargs.value = jmaki.filter(wargs.value, filter);
        }
        data = wargs.value;
        if ( wargs.value.columns) {
            cold =  wargs.value.columns;
        } 
        if (wargs.value.rows) {
            data = wargs.value.rows
        } else {
            data = wargs.value;
        }
        init();
    } else if (wargs.service) {
        jmaki.doAjax({url: wargs.service, callback: function(req) {
            if (req.readyState == 4) {
                if (req.status == 200) {
                    var _in = eval('(' + req.responseText + ')');
                   // convert value if a jmakiRSS type
                   if (_in.dataType == 'jmakiRSS') {
                       _in = jmaki.filter(_in, filter);
                    }
                    if (_in.rows) {
                        data = _in.rows;
                    }  else {
                        data = _in;
                    }
                    if (!cold && _in.columns) {
                        cold = _in.columns;
                    }
                    init();
                }
            }
        }});
    }
    
    // normalize the data based on the schema
    this.addRows = function(_d) {
        var rs = self.dataTable.getRecordSet();
        var nd = [];
        for (var i=0; i < _d.length; i++) {
            var row = {};
            for (var ii=0; ii < _d[i].length; ii++) {
                // this code allows for matching to the columin id with row data
                if (_d[i][ii].id) {
                    row[schema[_d[i][ii].id]] = _d[i][ii].value;
                } else {
                  row[schema[ii]] = _d[i][ii];
                }
            }
            nd.push(row);
        }
       rs.addRecords(nd);
       self.dataTable.populateTable();
    }
    
    this.addRow = function(_d) {
        var a= [];
        a.push(_d);
        self.addRows(a);
    }
    
    this.clear = function() {
      var rs = self.dataTable.getRecordSet();
      rs.reset();
      while (self.dataTable.getRow(0) != null) {
          self.dataTable.deleteRow(self.dataTable.getRow(0));
      }
      // the following for yui 2.2.2
      //self.dataTable.refreshTable();
    }
    

    function init() {
        // create the columns and editors
        for (var i = 0 ; i < cold.length; i++){
            var col = {};

            if (cold[i].id) {
               schema.push(cold[i].id);             
               schema[cold[i].id] = cold[i].id;
            }
            else {
                schema.push(cold[i].title);
                schema[cold[i].title] = cold[i].title;
            }
            // mix in everything but the renderer and editor
            for (var ii in cold[i]) {
                if (ii == 'editor') {
                    // create new editor with the mixins
                    col.editor = cold[i].editor;
                } else if (ii == 'renderer') {      
                    col.renderer = cold[i].renderer;
                } else if (ii == 'id') { 
                     col.key = cold[i].id;                    
                } else if (ii == 'title') {
                     if (typeof cold[i].id == "undefined") {
                         col.key = cold[i].title;
                     }
                     
                     col.text = cold[i].title;
                } else {
                    col[ii] = cold[i][ii];
                }
            }
            if (!col.sortable) col.sortable = true;
            cols.push(col);
        }
        
        // normalize data for yahoo which wants key value pairs for every row item
        for (var i=0; i < data.length; i++) {
            var row = {};
            for (var ii=0; ii < data[i].length; ii++) {
                // this code allows for matching to the columin id with row data
                if (data[i][ii].id) {
                    row[schema[data[i][ii].id]] = data[i][ii].value;
                } else {
                  row[schema[ii]] = data[i][ii];
                }
            }
            nData.push(row);
        }
        
        var columns = new YAHOO.widget.ColumnSet(cols);
        
        var ds = new YAHOO.util.DataSource(nData);
        ds.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
        ds.responseSchema = {fields :schema};
        
        
        var onCellEdit = function(oArgs) {
            alert("cel edit");
            YAHOO.log("Cell \"" + oArgs.target.id +
            "\" was updated from \"" + oArgs.oldData + "\" to \"" +
            oArgs.newData + "\"", "info", this.toString());
        }
        
        self.dataTable = new YAHOO.widget.DataTable(wargs.uuid, columns, ds,{fixedWidth: false,caption:""});
        self.dataTable.subscribe("cellClickEvent",self.dataTable.onEventEditCell);
        self.dataTable.subscribe("cellMouseoverEvent",self.dataTable.onEventHighlightCell);
        self.dataTable.subscribe("cellMouseoutEvent",self.dataTable.onEventUnhighlightCell);
        
        self.dataTable.subscribe("cellEditEvent",function() {alert("cell edit here")});
        self.dataTable.populateTable();
    }
}