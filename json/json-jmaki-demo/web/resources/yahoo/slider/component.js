// define the namespaces
jmaki.namespace("jmaki.widgets.yahoo.slider");

/**
 * Yahoo UI Horizontal/Vertical Slider Widget
 * Code originally adapted from yahoo.vslider
 * @author Ahmad M. Zawawi <ahmad.zawawi@gmail.com>
 * @see http://developer.yahoo.com/yui/slider/
 */
jmaki.widgets.yahoo.slider.Widget = function(wargs) {
    
    var topic = "/yahoo/slider";
    var slider;
    var uuid = wargs.uuid;
    var self = this;
    
    var sliderType = "H"; //default to horizontal (valid values: H for horizontal, V for vertical
    var sliderLeft = 0;     // The horizontal slider can move X pixels left/up
    var sliderRight = 200;  // The horizontal slider can move X pixels right/down
    var tickSize;  // Slider Tick Size
    var scaleFactor = 0.5; //Custom scale factor for converting pixel offset into a real value
    var animate = true; //animation on/off flag
    var initialValue; //The number of pixels from the start point
    
    //read the widget configuration arguments
    if (typeof wargs.args != 'undefined') {
        //overide topic name if needed
        if (typeof wargs.args.topic != 'undefined') {
            topic = wargs.args.topic;
        }  
        
        if (typeof wargs.args.sliderType != 'undefined') {
            sliderType = wargs.args.sliderType;
        }
        if (typeof wargs.args.sliderLeft != 'undefined') {
            sliderLeft = wargs.args.sliderLeft;
        }
        if (typeof wargs.args.sliderRight != 'undefined') {
            sliderRight = wargs.args.sliderRight;
        }
        if (typeof wargs.args.tickSize != 'undefined') {
            tickSize = wargs.args.tickSize;
        }
        if (typeof wargs.args.scaleFactor != 'undefined') {
            scaleFactor = wargs.args.scaleFactor;
        }
        if (typeof wargs.args.animate != 'undefined') {
            animate = wargs.args.animate;
        }
        if (typeof wargs.args.initialValue != 'undefined') {
            initialValue = wargs.args.initialValue;
        } else {
            initialValue = (sliderRight-sliderLeft) / 2;
        }
    }
    YAHOO.log("sliderType = " + sliderType);
    YAHOO.log("sliderLeft = " + sliderLeft);
    YAHOO.log("sliderRight = " + sliderRight );
    YAHOO.log("scaleFactor = " + scaleFactor );
    YAHOO.log("animate = " + animate );
    YAHOO.log("initialValue = " + initialValue );
    YAHOO.log("tickSize = " + tickSize );
    
    var isHorizSlider = (sliderType == 'H');
    var prefix = (isHorizSlider) ? 'horiz' : 'vert';
    YAHOO.log("prefix = " + prefix);
    
    var sliderDiv = uuid + "_" + prefix + "Slider";
    var sliderBgDiv = uuid + "_" + prefix + "BGDiv";
    var sliderHandleDiv = uuid + "_" + prefix + "HandleDiv";
    var sliderTextVal = uuid + "_" + prefix + "Val";
    var sliderForm = uuid + "_formV";
    var sliderButton = uuid + "_" + prefix + "btnsrc";
    this.init = function() {
        YAHOO.util.Event.addListener(sliderForm, "submit", self.updateHoriz);
        //YAHOO.util.Event.addListener(sliderButton, "click", this.updateHoriz);
            
        //create button from markup
        var button =  new YAHOO.widget.Button(sliderButton,
        { id: '_' + sliderButton, type: "button" });
        button.subscribe("click",self.updateHoriz);
        
        if(isHorizSlider) {
            //create horizontal slider
            slider = YAHOO.widget.Slider.getHorizSlider(
            sliderBgDiv,  sliderHandleDiv,  sliderLeft, sliderRight, tickSize);
        } else {
            //create vertical slider
            slider = YAHOO.widget.Slider.getVertSlider(
            sliderBgDiv,  sliderHandleDiv,  sliderLeft, sliderRight, tickSize);
        }
        
        slider.animate = animate; 
        slider.setValue(initialValue);
        
        //onChange event 
        onChange = function(offsetFromStart) {
            // use the scale factor to convert the pixel offset into a
            // real value
            var realValue = parseInt(offsetFromStart * scaleFactor);
            document.getElementById(sliderTextVal).value = realValue;
            document.getElementById(sliderBgDiv).title = "" + realValue;
            jmaki.publish(topic + "/onChange", {id:uuid, value:realValue}); 
        };
        slider.subscribe("change",onChange);
        
        //show hidden Horizontal/Veritcal slider
        document.getElementById(sliderDiv).style.display = "block";
        
    }
    
    /**
     * updateHoriz: called by the slider on button submit
     */
    this.updateHoriz = function() {
        var valueId = sliderTextVal;
        var v = parseFloat(document.getElementById(valueId).value, 10);
        if ( isNaN(v) )  { 
            v = 0; 
        }
        // convert the real value into a pixel offset
        slider.setValue(Math.round(v/scaleFactor));
        //var newVal = slider.getValue();
        //if(newVal != v) {
        //    document.getElementById(valueId).value = newVal;
        //}
        return false;
    }
    
    this.init();
    
}