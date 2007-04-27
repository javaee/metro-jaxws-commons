// define the namespaces
jmaki.namespace("jmaki.widgets.yahoo.tree");

/**
 * Yahoo UI Tree Widget
 * 
 * @author Gregory Murray <gregory.murray@sun.com> 
 *       (original author)
 * @author Ahmad M. Zawawi <ahmad.zawawi@gmail.com> 
 *       (Added proper glue/topic support with yahoo's new event model)
 *
 * @see http://developer.yahoo.com/yui/treeview/
 */
jmaki.widgets.yahoo.tree.Widget = function(wargs) {
    var self = this;
    var uuid = wargs.uuid;
    var topic = "/yahoo/tree";
    
    this.tree = new YAHOO.widget.TreeView(wargs.uuid);
    
    //Default on node expand handler
    this.tree.subscribe("expand", function(node) {
        var v = { label: node.label };
        jmaki.publish(topic + "/onExpand",{id:uuid,value:v});
    });
    
    //Default on node collapse handler
    this.tree.subscribe("collapse", function(node) {
        var v = { label: node.label };
        jmaki.publish(topic + "/onCollapse",{id:uuid,value:v});
    });
    
    //Default on node label click handler
    this.tree.subscribe("labelClick", function(node) {
        var v = { label: node.label };
        jmaki.publish(topic + "/onClick",{id:uuid,value:v});
    });
    
    //read the widget configuration arguments
    if (typeof wargs.args != 'undefined') {
        
        //overide topic name if needed
        if (typeof wargs.args.topic != 'undefined') {
            topic = wargs.args.topic;
        }  
    }
    
    // use the default tree found in the widget.json if none is provided
    if (!wargs.value ) {
        var callback;
        // default to the service in the widget.json if a value has not been st
        // and if there is no service
        if (typeof wargs.service == 'undefined') {
            wargs.service = wargs.widgetDir + "/widget.json";
            callback = function(req) {
                if (req.readyState == 4) {
                    var obj = eval("(" + req.responseText + ")");
                    var jTree = obj.value.data;
                    var root = jTree.root;
                    buildTree(root);
                }
            }
            
        } else {
            callback = function(req) {
                if (req.readyState == 4) {
                    var jTree = eval("(" + req.responseText + ")");
                    var root = jTree.root;
                    buildTree(root);
                }
            }        
        }
        var ajax = jmaki.doAjax({url : wargs.service, callback : callback});
    } else if (typeof wargs.value == 'object') {
        if (wargs.value.collapseAnim) {
            this.tree.setCollapseAnim(wargs.value.collapseAnim);
        }
        if (wargs.value.expandAnim) {
            this.tree.setExpandAnim(wargs.value.expandAnim);
        }
        buildTree(wargs.value.root);
    }
    
    var nodes = [];
    var nodeIndex;
    
    /**
     * Builds the tree programtically (recursively)
     */
    function buildTree(root, parent) {
        
        var rChildren = (typeof root.children != 'undefined');
        var rExpanded = (typeof root.expanded != 'undefined' &&  (root.expanded == true || root.expanded == "true"));
        
        if (typeof parent == 'undefined') {
            parent = self.tree.getRoot();
        }
        
        // Backwards compatibility -- copy "title" to "label" if needed
        // but we will use "label" henceforth
        if (root.title && !root.label) {
            root.label = root.title;
        }
        // End of backwards compatibility hack
        
        var rNode = new YAHOO.widget.TextNode(root.label, parent, rExpanded);
        for (t in root.children) {
            var n = root.children[t];
            var hasChildren = (typeof n.children != 'undefined');
            var isExpanded = (typeof n.expanded  != 'undefined' && n.expanded == "true");
            
            // Backwards compatibility -- copy "title" to "label" (as above)
            if (n.title && !n.label) {
                n.label = n.title;
            }
            // End of backwards compatibility hack
            var lNode = new YAHOO.widget.TextNode(n.label, rNode, isExpanded);
            
            //  recursively call this function to add children
            if (typeof n.children != 'undefined') {
                for (ts in n.children) {
                    buildTree(n.children[ts], lNode);
                }
            }
            
        }
        self.tree.draw();
    }
}
