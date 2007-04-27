// define the namespaces
jmaki.namespace("jmaki.widgets.yahoo.geocoder");

/**
 * Yahoo Geocoder Widget
 *      This widget lets you find geocordinates using the XMLHttpProxy using 
 *      the Yahoo Geocoder service
 *
 * @author Greg Murray 
 *          (original author)
 * @author Ahmad M. Zawawi <ahmad.zawawi@gmail.com>  
 *          (Updated to new yahoo jmaki widget standard)
 *
 * @see http://developer.yahoo.com/maps/rest/V1/geocode.html
 */
jmaki.widgets.yahoo.geocoder.Widget = function(wargs) {
    
    var topic = "/yahoo/geocoder";
    var uuid = wargs.uuid;
    var self = this;
    
    // we run on the xhp now
    var service = jmaki.xhp;
    
    //overide topic name if needed
    if (wargs.args && wargs.args.topic) {
        topic = wargs.args.topic;
    }
    
    
    
    var location; 
    
    /**
     */
    this.getCoordinates = function() {
        location = encodeURIComponent(document.getElementById(uuid + "_location").value);
        var encodedLocation = encodeURIComponent("location=" + location);        
        var url = service + "?id=yahoogeocoder&urlparams=" + encodedLocation;        
        jmaki.doAjax({url: url, callback: function(req) { var _req=req; postProcess(_req);}});
    }
    
    /**
     */
    function postProcess(req) {
        if (req.readyState == 4) {
            if (req.status == 200) {
                var v = {success:false};
                if(req.responseText.length > 0) {
                    var response = eval("(" + req.responseText + ")");
                    var coordinates = response.coordinates;
                    v = {success:true,results:coordinates};
                    
                    //for compatibility (deprecated): we leave this one for a while
                    jmaki.publish(topic, coordinates);  
                } 
                //the new format is here (as in v)
                //with status flag sent
                jmaki.publish(topic + "/onGeocode", {id: uuid, value:v} )                
            } 
        }
        
    }
    
}
