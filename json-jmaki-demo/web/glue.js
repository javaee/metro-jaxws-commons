/*
 * These are some predefined glue listeners that you can
 *  modify to fit your application.
 *
 * This file should not placed in the /resources directory of your application
 * as that directory is for jmaki specific resources.
 */

 // defind the name space for the listeners.
jmaki.namespace("jmaki.listeners");

jmaki.listeners.handleFisheye = function(args) {
        alert("glue.js : fisheye event");
}
// map topic dojo/fisheye to fisheye handler
jmaki.addGlueListener(new RegExp("^(?!/global).*/dojo/fisheye"), "jmaki.listeners.handleFisheye");

jmaki.listeners.onSave = function(args) {
        alert("glue.js : onSave request from: " + args.id + " value=" + args.value);
}

// map topics ending with  /onSave to the handler
jmaki.addGlueListener(new RegExp("^(?!/global).*onSave$"), "jmaki.listeners.onSave");

jmaki.listeners.debug = function(args) {
        alert("debug: " + args);
}
// map the topics ending with /debug 
jmaki.addGlueListener(new RegExp("^(?!/global).*/debug$"), "jmaki.listeners.debug");