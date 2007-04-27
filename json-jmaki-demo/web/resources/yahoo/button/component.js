// define the namespaces
jmaki.namespace("jmaki.widgets.yahoo.button");

/**
 * Yahoo UI Button Widget
 * @author Ahmad M. Zawawi <ahmad.zawawi@gmail.com>
 * @see http://developer.yahoo.com/yui/button/
 */
jmaki.widgets.yahoo.button.Widget = function(wargs) {
    
    var topic = "/yahoo/button";
    var self = this;
    var button;
    var buttonGroup;
    var uuid = wargs.uuid; 
    var buttonId = uuid + "_btn";
    var containerId = uuid + "_btnDiv";
    
    YAHOO.log("buttonId = " + buttonId);
    
    //button default event handlers
    this.onClick = function() {
        jmaki.publish(topic + "/onClick", {id:uuid,value:{}});
    }
    
    this.onChange = function(e) {
        jmaki.publish(topic + "/onChange", {id:uuid,
        value:{oldValue:e.prevValue,newValue:e.newValue}});
    }
    
    //Configuration
    var cfg = {
        name: buttonId,
        type: "button",
        label: "Click me",
        href: "http://ajax.dev.java.net",
        checked: false,
        val: "",
        container: containerId,
        buttons: [
        { label:'One', value:'1'},
        { label:'Two', value:'2', checked:true }, 
        { label:'Three', value:'3' }, 
        { label:'Four', value:'4' }            
        ]
    };
    
    //read the widget configuration arguments
    if (typeof wargs.args != 'undefined') {

        //overide topic name if needed
        if (typeof wargs.args.topic != 'undefined') {
            topic = wargs.args.topic;
        }  
        
        if (typeof wargs.args.name != 'undefined') {
            cfg.name = wargs.args.name;
        }
        if (typeof wargs.args.val != 'undefined') {
            cfg.val = wargs.args.val;
        }
        if (typeof wargs.args.type != 'undefined') {
            var btnTypes = {
                'button':'button','link':'link','submit':'submit',
                'reset':'reset','checkbox':'checkbox',
                'radio':'radio'
            };
            var t = btnTypes[wargs.args.type];
            cfg.type = (typeof t != 'undefined') ? t : "button";
        }
        
        if (typeof wargs.args.label != 'undefined') {
            cfg.label = wargs.args.label;
        }
        if (typeof wargs.args.href != 'undefined') {
            cfg.href = wargs.args.href;
        }
        if (typeof wargs.args.checked != 'undefined') {
            cfg.checked = wargs.args.checked;
        }        
        if (typeof wargs.args.container != 'undefined') {
            cfg.container = wargs.args.container;
        }
    }
    if (typeof wargs.value != 'undefined') {
        var v = wargs.value;
        if(typeof v.buttons != 'undefined') {
            cfg.buttons = v.buttons;
        }
    }       
    
    YAHOO.log("name = " + cfg.name);
    YAHOO.log("val = " + cfg.val);
    YAHOO.log("type = " + cfg.type);
    YAHOO.log("label = " + cfg.label);
    YAHOO.log("href = " + cfg.href);
    YAHOO.log("checked = " + cfg.checked);
    YAHOO.log("container = " + cfg.container);
    
    
    /**
     * initialize the button
     */
    this.init = function() {
        // Create the button
        if(cfg.type == 'radio') {
            //create radio button group (0..n) buttons
            buttonGroup = new YAHOO.widget.ButtonGroup({ 
                id: cfg.buttonId, 
                name: cfg.name, 
                container:cfg.container
            });
            //create buttons (runtime) and add them
            for(var i = 0; i < cfg.buttons.length; i++)  {
                var b = cfg.buttons[i];
                var checked = (b.checked) ? true : false;
                var btn = { label:b.label, value:b.value, checked:checked };
                var button = buttonGroup.addButton(btn);
                button.addListener("click", self.onClick);
            }
        } else {
            //create normal button
            button = new YAHOO.widget.Button({
                id: buttonId, 
                name: cfg.name,
                value: cfg.val,
                type: cfg.type, 
                label: cfg.label,
                checked: cfg.checked,
                href: cfg.href,
                container: cfg.container
            });    
            
            
            if(cfg.type == "checkbox")  {
                button.addListener("checkedChange", self.onChange);
            }
            button.addListener("click", self.onClick);
        }
        
    }
    
    /**
     * Returns the checked status of a checkbox
     */
    this.isChecked = function() {
        return (typeof button  != 'undefined') ? 
            button.get("checked") : buttonGroup.get("checked");
    }
    
    /**
     * Returns the current value of the button
     */
    this.getVal = function() {
        return (typeof button  != 'undefined') ? 
            button.get("value") : buttonGroup.get("value");
    }
    
    /**
     * Sets the disabled property for a button
     */
    this.setDisabled = function(disabled) {
        if(typeof button  != 'undefined') {
            button.set("disabled",disabled);
        } else {
            buttonGroup.set("disabled",disabled);
        }
    }
    
    this.init();
    
}