// define the namespaces
jmaki.namespace("jmaki.widgets.yahoo.map");

/*
 * Yahoo Map Wrapper
 * This wrapper is for version 3.4 of yahoo maps api as described at:
 *
 * @author Greg Murray  (original author)
 * @author Ahmad M. Zawawi <ahmad.zawawi@gmail.com> (Added YEvent jMaki publish events)
 *
 * @see http://developer.yahoo.com/maps/ajax/V3.4/reference.html
 */ 
jmaki.widgets.yahoo.map.Widget = function(wargs) {
    var topic = "/yahoo/map";
    var self = this;
    var uuid = wargs.uuid;
    
    this.zoom = 7;
    var autoSizeH = true;
    var autoSizeW = true;
    // default location to Yahoo
    var centerLat = 37.4041960114344;
    var centerLon = -122.008194923401;
    var centerPoint;
    
    var mapType = YAHOO_MAP_SAT;
    var VIEWPORT_HEIGHT = 0;
    var VIEWPORT_WIDTH = 0;
    this.map;
    var oldResize;
    var oldWidth;
    // we need this for resize eventss
    var ie = /MSIE/i.test(navigator.userAgent);
    var safari = /Safari/i.test(navigator.userAgent);
    
    // pull in args
    if (typeof wargs.args != 'undefined') {
        
        //overide topic name if needed
        if (typeof wargs.args.topic != 'undefined') {
            topic = wargs.args.topic;
        }  
        
        if (typeof wargs.args.zoom != 'undefined') {
            this.zoom = Number(wargs.args.zoom);
        }
        
        if (typeof wargs.args.centerLat != 'undefined') {
            centerLat = Number(wargs.args.centerLat);
        }
        
        if (typeof wargs.args.centerLon != 'undefined') {
            centerLon = Number(wargs.args.centerLon);
        }
        
        if (typeof wargs.args.mapType != 'undefined') {
            
            if (wargs.args.mapType == 'REGULAR') {
                mapType = YAHOO_MAP_REG;
            } else if (wargs.args.mapType == 'SATALITE') {
                mapType = YAHOO_MAP_SAT;
            } else if (wargs.args.mapType == 'HYBRID') {
                mapType = YAHOO_MAP_HYB;
            }
        }       
        if (typeof wargs.args.height != 'undefined') {
            VIEWPORT_HEIGHT = Number(wargs.args.height);
            autoSizeH = false;
        }
        
        if (typeof wargs.args.width != 'undefined') {
            VIEWPORT_WIDTH = Number(wargs.args.width);
            autoSizeW = false;
        }
    }
    var _container = document.getElementById(uuid);
    
    var mapSize = new YSize(VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
    centerPoint = new YGeoPoint(centerLat,centerLon);
    this.map = new YMap(_container, mapType, mapSize);
    //this.map.drawZoomAndCenter(centerPoint, this.zoom);
    
    // Add this.map type control
    this.map.addTypeControl();
    
    // this.map zoomer
    this.map.addZoomLong();
    
    // Set this.map type to either of: YAHOO_MAP_SAT YAHOO_MAP_HYB YAHOO_MAP_REG
    
    this.map.setMapType(mapType);
    
    
    /**
     * Just a utility class to convert from yahoo's to ours
     */
    function evt2Value(e) {
        return { 
            lat:e.YGeoPoint.Lat, 
            lon:e.YGeoPoint.Lon, 
            prevZoom:e.zoomObj.previous, 
            currZoom:e.zoomObj.current 
        };
    }
    //on click map default handler
    YEvent.Capture(this.map,EventsList.MouseClick, function(e) {
        jmaki.publish(topic + "/onClick", {id:uuid, value:evt2Value(e)});
    });
    //on change zoom default handler
    YEvent.Capture(this.map,EventsList.changeZoom ,function(e) {
        jmaki.publish(topic + "/onChangeZoom", {id:uuid, value:evt2Value(e)});
    });
    
    /**
     */
    function getPosition(_e) {
        var pX = 0;
        var pY = 0;
        
        while (_e.offsetParent) {
            pY += _e.offsetTop;
            pX += _e.offsetLeft;
            _e = _e.offsetParent;
        }
        return {x: pX, y: pY};
    }
    
    /**
     */
    function resize() {
        
        if (oldResize) {
            oldResize();
        }
        if (autoSizeH || autoSizeW){
            var pos = getPosition(_container);
            if (_container.parentNode.nodeName == "BODY") {
                if (window.innerHeight){
                    if (autoSizeH) VIEWPORT_HEIGHT = window.innerHeight - pos.y -16;
                    if (autoSizeW) VIEWPORT_WIDTH = window.innerWidth - 15;
                } else {
                    var _tNode = _container.parentNode;
                    while(_tNode != null &&
                    (_tNode.clientHeight == 0 ||
                    typeof _tNode.clientWidth == 'undefined')) {
                        _tNode = _tNode.parentNode;
                    }
                    if (_tNode == null) {
                        VIEWPORT_WIDTH = 400;
                    } else {
                        if (autoSizeW) VIEWPORT_WIDTH = _tNode.clientWidth - 20;
                        if (autoSizeH) VIEWPORT_HEIGHT = _tNode.clientHeight - pos.y - 15;
                    }
                }
                if (VIEWPORT_HEIGHT < 0) {
                    VIEWPORT_HEIGHT = 300;
                }
                if (VIEWPORT_WIDTH < 0) {
                    VIEWPORT_WIDTH = 400;
                }
            } else {
                var _tNode = _container.parentNode;
                while(_tNode != null &&
                (_tNode.clientHeight == 0 ||
                typeof _tNode.clientWidth == 'undefined')) {
                    _tNode = _tNode.parentNode;
                }
                if (_tNode == null) {
                    if (autoSizeW) VIEWPORT_WIDTH = 400;
                } else {
                    if (autoSizeW)  VIEWPORT_WIDTH = _tNode.clientWidth;
                    if (autoSizeH)  VIEWPORT_HEIGHT = _tNode.clientHeight;
                    if (safari) {
                        VIEWPORT_WIDTH -= 18;
                        VIEWPORT_HEIGHT -= 18;
                    }
                }
            }                  
        }
        
        _container.style.width = VIEWPORT_WIDTH + "px";
        _container.style.height = VIEWPORT_HEIGHT + "px";
        var mapSize = new YSize(VIEWPORT_WIDTH, VIEWPORT_HEIGHT);
        self.map.resizeTo(mapSize);
        oldWidth = document.body.clientWidth;
        // Display the this.map centered on a latitude and longitude
        if (typeof self.map != 'undefined') {
            self.map.drawZoomAndCenter(centerPoint, self.zoom);
        }
    }
    
    var resizing = false;
    var lastSize = 0;
    
    /**
     */
    function layout() {
        if (!ie) {
            resize();
            return;
        }
        // special handling for ie resizing.
        // we wait for no change for a full second before resizing.
        if (oldWidth != document.body.clientWidth && !resizing) {
            if (!resizing) {
                resizing = true;
                setTimeout(layout,1000);
            }
        } else if (resizing && document.body.clientWidth == lastSize) {
            resizing = false;
            resize();
        } else if (resizing) {
            lastSize = document.body.clientWidth;
            setTimeout(layout,1000);
        }
    }
    
    resize();
    if (typeof window.onresize != 'undefined') {
        oldResize = window.onresize;
    }
    window.onresize = layout; 
}