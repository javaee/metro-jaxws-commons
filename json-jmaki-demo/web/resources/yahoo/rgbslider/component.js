// define the namespaces
jmaki.namespace("jmaki.widgets.yahoo.rgbslider");

/**
 * Yahoo RGB/HSV Slider jMaki Widget
 *
 * This has been adapted and modified from the RGB/HSV Slider 
 * example found in YUI 2.2
 *
 * @author Ahmad M. Zawawi <ahmad.zawawi@gmail.com>
 * @see http://developer.yahoo.com/yui/slider/
 */
jmaki.widgets.yahoo.rgbslider.Widget = function(wargs) {
    
    var topic = "/yahoo/rgbslider";
    var self = this;
    var uuid = wargs.uuid; 
    
    var sliderType = "RGB"; //default to RGB (valid values: RGB, HSV)
    //read the widget configuration arguments
    if (typeof wargs.args != 'undefined') {
        //overide topic name if needed
        if (typeof wargs.args.topic != 'undefined') {
            topic = wargs.args.topic;
        }  
        
        if (typeof wargs.args.sliderType != 'undefined') {
            sliderType = wargs.args.sliderType;
        }
    }
    
    YAHOO.log("sliderType = " + sliderType);
    
    var rgbSliderId = uuid + '_rgbslider';
    var hsvSliderId = uuid + '_hsvslider';
    
    YAHOO.log("rgbSliderId = " + rgbSliderId);
    YAHOO.log("hsvSliderId = " + hsvSliderId);
    
    var Event = YAHOO.util.Event;
    var Dom   = YAHOO.util.Dom;
    var Slider = YAHOO.widget.Slider;
    var Button = YAHOO.widget.Button;
    
    var pickerSize=180;
    var hue,picker,panel;    
    var r, g, b, dd;
    var hexchars = '0123456789ABCDEF';
    /**
     * Code taken from Yahoo.util.Color
     */
    function rgb2hex(r,g,b) {
        return int2hex(r) + int2hex(g) + int2hex(b);
    }
    
    /**
     * Code taken from Yahoo.util.Color
     * Converts an int [0,255] to hex [00,FF]
     */
    function int2hex(n) {
        n = n || 0;
        n = parseInt(n, 10);
        if (isNaN(n)) { n = 0; }
        n = Math.round(Math.min(Math.max(0, n), 255));
        
        return hexchars.charAt((n - n % 16) / 16) + hexchars.charAt(n % 16);
    }
    
    /**
     * Code taken from Yahoo.util.Color
     */
    function hex2dec(hexchar) {
        return hexchars.indexOf(hexchar.toUpperCase());
    }
    
    /**
     * Code taken from Yahoo.util.Color
     */
    function hex2rgb(s) { 
        var rgb = [];
        rgb[0] = (hex2dec(s.substr(0, 1)) * 16) + hex2dec(s.substr(1, 1));
        rgb[1] = (hex2dec(s.substr(2, 1)) * 16) + hex2dec(s.substr(3, 1));
        rgb[2] = (hex2dec(s.substr(4, 1)) * 16) + hex2dec(s.substr(5, 1));
        return rgb;
    }
    
    /**
     * Code taken from Yahoo.util.Color
     */
    function isValidRGB(a) { 
        if ((!a[0] && a[0] !=0) || isNaN(a[0]) || a[0] < 0 || a[0] > 255) { return false; }
        if ((!a[1] && a[1] !=0) || isNaN(a[1]) || a[1] < 0 || a[1] > 255) { return false; }
        if ((!a[2] && a[2] !=0) || isNaN(a[2]) || a[2] < 0 || a[2] > 255) { return false; }
        return true;
    }
    
    /**
     * Code taken from Yahoo.util.Color
     */
    function real2int(n) {
        return Math.min(255, Math.round(n*256));
    };
    
    /**
     * HSV to RGB. h[0,360], s[0,1], v[0,1]
     */
    function hsv2rgb(h,s,v) { 
        var r,g,b,i,f,p,q,t;
        i = Math.floor((h/60)%6);
        f = (h/60)-i;
        p = v*(1-s);
        q = v*(1-f*s);
        t = v*(1-(1-f)*s);
        switch(i) {
            case 0: r=v; g=t; b=p; break;
            case 1: r=q; g=v; b=p; break;
            case 2: r=p; g=v; b=t; break;
            case 3: r=p; g=q; b=v; break;
            case 4: r=t; g=p; b=v; break;
            case 5: r=v; g=p; b=q; break;
        }
        return [real2int(r), real2int(g), real2int(b)];
    }    
    
    /**
     */
    function updateSliderColors() {
        
        var curr, curg, curb;
        
        curr = Math.min(r.getValue() , 255);
        curg = Math.min(g.getValue() , 255);
        curb = Math.min(b.getValue() , 255);
        
        YAHOO.log("updateSliderColor " + curr + ", " + curg + ", " + curb);
        
        Dom.setStyle(uuid + "_rgbswatch", "background-color", 
        "rgb(" + curr + "," + curg + "," + curb + ")");
        
        Dom.get(uuid + "_hexval").value = rgb2hex(curr, curg, curb);
        
        jmaki.publish(topic + "/onRGBChange", {id:uuid, value:{r:curr,g:curg,b:curb}} );
    }
    
    /**
     */
    function listenerUpdate(whichSlider, newVal) {
        newVal = Math.min(255, newVal);
        Dom.get(uuid + "_" + whichSlider + "val").value = newVal;
        updateSliderColors();
    }
    
    /**
     */
    this.userReset = function() {
        var v;
        var f = document.forms[uuid + "_rgbform"];
        
        r.setValue(0,true);
        g.setValue(0,true);
        b.setValue(0,true);
    }
    
    /**
     * Initialize the YUI-style buttons
     */
    this.initButtons = function() {
        //from markup
        var button;
        button = new Button(uuid + "_rgbSubmitsrc",{ id: uuid + "_rgbSubmit", type: "button" });    
        button = new Button(uuid + "_hexSubmitsrc", { id: uuid + "_hexSubmit", type: "button"  });
        button = new Button(uuid + "_resetButtonsrc", { id: uuid + "_resetButton", type: "button" });
    }
    
    /**
     * Create the RGB Slider
     */
    this.rgbInit = function() {
        //red slider
        r = Slider.getHorizSlider(uuid + "_rBG", uuid + "_rthumb", 0, 255);
        r.subscribe("change", function(newVal) { listenerUpdate("r", newVal); });
        //green slider
        g = Slider.getHorizSlider(uuid + "_gBG", uuid + "_gthumb", 0, 255);
        g.subscribe("change", function(newVal) { listenerUpdate("g", newVal); });
        //blue slider
        b = Slider.getHorizSlider(uuid + "_bBG", uuid + "_bthumb", 0, 255);
        b.subscribe("change", function(newVal) { listenerUpdate("b", newVal); });
        
        self.initButtons();
        self.initColor();
        
        //enable Drag-n-Drop support for rgb slider
        dd = new YAHOO.util.DD(rgbSliderId);
        dd.setHandleElId(uuid + "_pickerHandle");
        
        //show hidden rgb slider
        Dom.setStyle(rgbSliderId,"display","block");
    }
    
    
    /**
     * initalize the slider colours and buttons
     */
    this.initColor =  function() {
        d = document.createElement("P");
        d.className = "rb";
        r.getEl().appendChild(d);
        d = document.createElement("P");
        d.className = "rb";
        g.getEl().appendChild(d);
        d = document.createElement("P");
        d.className = "rb";
        b.getEl().appendChild(d);
        
        for (var i=0; i<65; i++) {
            var rBGId = uuid + "_rBG" + i;
            var gBGId = uuid + "_gBG" + i;
            var bBGId = uuid + "_bBG" + i;
            d = document.createElement("span");
            d.id = rBGId;
            r.getEl().appendChild(d);
            
            d = document.createElement("span");
            d.id = gBGId;
            g.getEl().appendChild(d);
            
            d = document.createElement("span");
            d.id = bBGId;
            b.getEl().appendChild(d);
            
            Dom.setStyle(rBGId, "background-color", 
            "rgb(" + (i*4) + ",0,0)");
            
            Dom.setStyle(gBGId, "background-color", 
            "rgb(0," + (i*4) + "," + "0)");
            
            Dom.setStyle(bBGId, "background-color", 
            "rgb(0,0," + (i*4) + ")");
        }
        
        d = document.createElement("p");
        d.className = "lb";
        r.getEl().appendChild(d);
        d = document.createElement("p");
        d.className = "lb";
        g.getEl().appendChild(d);
        d = document.createElement("p");
        d.className = "lb";
        b.getEl().appendChild(d);
        
        self.userUpdate();
    }
    
    /**
     * User has updated Hex color value via textfields
     */
    this.hexUpdate =  function(e) {
        return self.userUpdate(e, true);
    }
    
    /**
     * User has updated RGB color values via textfields
     */
    this.userUpdate = function(e, isHex) {
        var v;
        var f = document.forms[uuid + "_rgbform"];
        
        if (isHex) {
            var hexval = f[uuid + "_hexval"].value;
            // shorthand #369
            if (hexval.length == 3) {
                var newval = "";
                for (var i=0;i<3;i++) {
                    var a = hexval.substr(i, 1);
                    newval += a + a;
                }
                hexval = newval;
            }
            
            YAHOO.log("hexval:" + hexval);
            
            if (hexval.length != 6) {
                alert("illegal hex code: " + hexval);
            } else {
                var rgb = hex2rgb(hexval);
                YAHOO.log(rgb.toString());
                if (isValidRGB(rgb)) {
                    f[uuid + "_rval"].value = rgb[0];
                    f[uuid + "_gval"].value = rgb[1];
                    f[uuid + "_bval"].value = rgb[2];
                }
            }
        }
        
        // red
        v = parseFloat(f[uuid + "_rval"].value);
        v = ( isNaN(v) ) ? 0 : Math.round(v);
        YAHOO.log("setValue, r: " + v);
        r.setValue(Math.round(v),true);
        
        //green
        v = parseFloat(f[uuid + "_gval"].value);
        v = ( isNaN(v) ) ? 0 : Math.round(v);
        YAHOO.log("setValue, g: " + g);
        g.setValue(Math.round(v),true);
        
        //blue
        v = parseFloat(f[uuid + "_bval"].value);
        v = ( isNaN(v) ) ? 0 : Math.round(v);
        YAHOO.log("setValue, b: " + b);
        b.setValue(Math.round(v),true);
        
        
        updateSliderColors();
        
        if (e) {
            Event.stopEvent(e);
        }
    }
    
    /**
     * Update color hsv swatch [preview pane]
     */
    this.hsvSwatchUpdate = function() {
        var h=getH(), s=getS(), v=getV();
        YAHOO.log("hsv " + [h,s,v]);
        
        var hue = h;
        var sat = Math.round(s*100);
        var val = Math.round(v*100);
        Dom.get(uuid + "_hsv_hval").value = hue;
        Dom.get(uuid + "_hsv_sval").value = sat;
        Dom.get(uuid + "_hsv_vval").value = val;
        
        var rgb = hsv2rgb(h, s, v);
        
        var styleDef = "rgb(" + rgb.join(",") + ")";
        Dom.setStyle(uuid + "_hsvswatch", "background-color", styleDef);
        
        Dom.get(uuid + "_hsv_rval").value = rgb[0];
        Dom.get(uuid + "_hsv_gval").value = rgb[1];
        Dom.get(uuid + "_hsv_bval").value = rgb[2];
        
        Dom.get(uuid + "_hsv_hexval").value = rgb2hex(rgb[0], rgb[1], rgb[2]);
        
        jmaki.publish(topic + "/onHSVChange", {id:uuid, value:{h:hue,s:sat,v:val}} );
    };
    
    /**
     * Update Hue
     */
    this.hueUpdate = function(newOffset) {
        YAHOO.log("hue update: " + newOffset);
        var rgb = hsv2rgb(getH(), 1, 1);
        var styleDef = "rgb(" + rgb.join(",") + ")";
        Dom.setStyle(uuid + "_pickerDiv", "background-color", styleDef);
        
        self.hsvSwatchUpdate();
    };
    
    /**
     * Update picker
     */
    this.pickerUpdate = function(newOffset) {
        YAHOO.log("picker update [" + newOffset.x + ", " + newOffset.y + "]");
        self.hsvSwatchUpdate();
    };

    
    /**
     * hue, int[0,359]
     */
    function getH() {
        var h = (pickerSize - hue.getValue()) / pickerSize;
        h = Math.round(h*360);
        return (h == 360) ? 0 : h;
    }
    
    /**
     * saturation, int[0,1], left to right
     */
    function getS() {
        return picker.getXValue() / pickerSize;
    }
    
    /**
     * value, int[0,1], top to bottom
     */
    function getV() {
        return (pickerSize - picker.getYValue()) / pickerSize;
    }
    
    /**
     * Initialize the RGB Slider with its events
     */
    this.initRgb = function() {
        self.rgbInit();
        Event.on(uuid + "_rgbForm", "submit", self.userUpdate);
        Event.on(uuid + "_rgbSubmit", "click", self.userUpdate);
        Event.on(uuid + "_hexSubmit", "click", self.hexUpdate, this, true);
        Event.on(uuid + "_resetButton", "click", self.userReset);
    }
    
    /**
     * Initialize HSV Slider
     */
    this.initHsv = function () {
        hue = Slider.getVertSlider(uuid + "_hueBg", uuid + "_hueThumb", 0, pickerSize);
        hue.subscribe("change", self.hueUpdate);
        
        picker = Slider.getSliderRegion(uuid + "_pickerDiv", uuid + "_selector", 
        0, pickerSize, 0, pickerSize);
        
        picker.subscribe("change", self.pickerUpdate);
        
        
        self.hueUpdate(0); 
        
        panel = new YAHOO.util.DD(hsvSliderId);
        panel.setHandleElId(uuid + "_pickerHandle");
        
        //show hidden rgb slider
        Dom.setStyle(hsvSliderId,"display","block");
    }
    
    //initialize when content loaded & ready
    if(sliderType == 'RGB') {
        YAHOO.util.Event.onContentReady(rgbSliderId, self.initRgb);
    } else {
        YAHOO.util.Event.onContentReady(rgbSliderId, self.initHsv);
    }
    
    /**
     * Returns the rgb values as JSON structure
     */
    this.getRGB = function() {
        var result = {};
        if(sliderType == 'RGB') {
            //RGB
            result = {
                r: Math.min(r.getValue(), 255), 
                g: Math.min(g.getValue(), 255),
                b: Math.min(b.getValue(), 255)
            };
        } else {
            //HSV=>RGB
            var h=getH(), s=getS(), v=getV();
            var rgb = hsv2rgb(h, s, v);
            result = { r: rgb[0],  g: rgb[1], b: rgb[2] };            
        }
        return result;
    }
    
    /**
     * Returns the hsv values as JSON structure
     */
    this.getHSV = function() {
        var result = {};
        if(sliderType == 'HSV') {
            //HSV=>RGB
            var h=getH(), s=getS(), v=getV();
            var hue = h;
            var sat = Math.round(s*100);
            var val = Math.round(v*100);
            result = { h: hue,  s: sat, v: val };
        } else {
            YAHOO.log("This method is not yet implemented for RGB Sliders","warn");
        }
        return result;
    }
    
    /**
     * Sets the red,green and blue values
     * Skipping slider animation
     */
    this.setRGB = function(red,green,blue) {
        if(sliderType == 'RGB') {
            var f = document.forms[uuid + "_rgbform"];
            f[uuid + "_rval"].value = Math.min(red, 255);
            f[uuid + "_gval"].value = Math.min(green, 255);
            f[uuid + "_bval"].value = Math.min(blue, 255);
            self.userUpdate();
        } else {
            YAHOO.log("This method is not yet implemented for RGB Sliders","warn");
        }
    }

    
}