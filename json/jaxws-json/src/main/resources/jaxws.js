
    init : function () {
        if (window.XMLHttpRequest) {
            return new XMLHttpRequest();
        } else if (window.ActiveXObject) {
            return new ActiveXObject("Microsoft.XMLHTTP");
        }
    },

    post : function(obj, func) {
        var req = this.init();
        req.onreadystatechange = function() {
            if (req.readyState == 4) {
                if (req.status == 200) {
                    func(eval(req.responseText));
                } else {
                    alert("Error:"+req.status+":"+req.statusText);
                }
            }
        };
        req.open("POST", this.url, true);
        req.setRequestHeader("Content-Type", "application/json");
        req.send(obj);
    },

