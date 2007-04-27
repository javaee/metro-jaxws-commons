// define the namespaces
jmaki.namespace("jmaki.widgets.yahoo.simpledlg");

/**
 * Yahoo UI Simple Dialog Widget
 * @author Ahmad M. Zawawi <ahmad.zawawi@gmail.com>
 * @see http://developer.yahoo.com/yui/container/simpledialog/
 */
jmaki.widgets.yahoo.simpledlg.Widget = function(wargs) {
    
    var topic = "/yahoo/simpledlg";
    var dlg;
    var uuid = wargs.uuid;
    var dlgId = uuid + "_simpledlg";
    YAHOO.log("dlgId = " + dlgId);
    
    //dialog default event handlers
    function onClick(evt) {
        var el = YAHOO.util.Event.getTarget(evt);
        var v = { label:el.innerHTML };
        jmaki.publish(topic + "/onClick", {id:uuid,value:v});
        this.hide();
    };        
    
    //Configuration
    var cfg = {
        width: "300px",
        fixedcenter: true,
        header: "Header Text",
        text: "Body Text",
        draggable: true,
        close: true,
        visible: true,
        modal: false,
        icon: YAHOO.widget.SimpleDialog.ICON_INFO,
        constraintoviewport: true,
        buttons: [ 
        { label:"Yes" },
        { label:'No', isDefault:true }
        ]
    };
    
    
    //read the widget configuration arguments
    if (typeof wargs.args != 'undefined') {
        
        //overide topic name if needed
        if (typeof wargs.args.topic != 'undefined') {
            topic = wargs.args.topic;
        }         
        if (typeof wargs.args.width != 'undefined') {
            cfg.width = wargs.args.width;
        }
        if (typeof wargs.args.fixedcenter != 'undefined') {
            cfg.fixedcenter = wargs.args.fixedcenter;
        }
        if (typeof wargs.args.header != 'undefined') {
            cfg.header = wargs.args.header;
        }
        if (typeof wargs.args.text != 'undefined') {
            cfg.text = wargs.args.text;
        }
        if (typeof wargs.args.draggable != 'undefined') {
            cfg.draggable = wargs.args.draggable;
        }
        if (typeof wargs.args.close != 'undefined') {
            cfg.close = wargs.args.close;
        }
        if (typeof wargs.args.visible != 'undefined') {
            cfg.visible = wargs.args.visible;
        }
        if (typeof wargs.args.modal != 'undefined') {
            cfg.modal = wargs.args.modal;
        }       
        if (typeof wargs.args.constraintoviewport != 'undefined') {
            cfg.constraintoviewport = wargs.args.constraintoviewport;
        }               
        if (typeof wargs.args.icon != 'undefined') {
            var icTypes = {
                'ALARM':YAHOO.widget.SimpleDialog.ICON_ALARM,
                'BLOCK':YAHOO.widget.SimpleDialog.ICON_BLOCK, 
                'HELP':YAHOO.widget.SimpleDialog.ICON_HELP, 
                'INFO':YAHOO.widget.SimpleDialog.ICON_INFO, 
                'TIP':YAHOO.widget.SimpleDialog.ICON_TIP, 
                'WARN':YAHOO.widget.SimpleDialog.ICON_WARN
            };
            var t = icTypes[wargs.args.icon];
            cfg.icon = (typeof t != 'undefined') ? t : YAHOO.widget.SimpleDialog.ICON_INFO;
        }
        
        
    }
    if (typeof wargs.value != 'undefined') {
        var v = wargs.value;
        if(typeof v.buttons != 'undefined') {
            cfg.buttons = v.buttons;
        }
    }       
    
    YAHOO.log("width = " + cfg.width);
    YAHOO.log("fixedcenter = " + cfg.fixedcenter);
    YAHOO.log("header = " + cfg.header);
    YAHOO.log("text = " + cfg.text);
    YAHOO.log("draggable = " + cfg.draggable);
    YAHOO.log("close = " + cfg.close);
    YAHOO.log("visible = " + cfg.visible);
    YAHOO.log("icon = " + cfg.icon);
    YAHOO.log("modal = " + cfg.modal);
    
    /**
     * Create Yahoo Buttons
     */
    function createYButtons(buttons) {
        var ybs = [];
        for(var i = 0; i < buttons.length; i++) {
            var btn = buttons[i];
            if(typeof btn != 'undefined') {
                var yb = {};
                if(typeof btn.label != 'undefined' ) {
                    yb.text = btn.label;
                }
                if(typeof btn.isDefault != 'undefined') {
                    yb.isDefault = btn.isDefault;
                }
                yb.handler = onClick;
                ybs.push(yb);
            } else {
                YAHOO.log('Found an empty button defintion',"warn");
            }
        }
        return ybs;
    }
    
    /**
     * initialize the simple dialog
     */
    this.init = function() {
        //override our simple button data format 
        //with new yahoo data button format
        cfg.buttons = createYButtons(cfg.buttons);
        
        // Create the Simple Dialog
        dlg =  new YAHOO.widget.SimpleDialog("dlg" + uuid, cfg);
        dlg.setHeader(cfg.header); 
        dlg.render(document.body); 
    }
    
    /**
     *  shows or hides the dialog depending on the visible flag
     */
    this.setVisible = function(visible) {
        if(visible) {
            dlg.show();
        } else {
            dlg.hide();
        }
    }
    
    this.init();
    
}