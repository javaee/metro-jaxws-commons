<%@ taglib prefix="a" uri="http://jmaki/v1.0/jsp" %>
<html>
  <body>
    <a:widget name="yahoo.dataTable" id='table' args="{columns:[
              {id:'title'}, 
              {id:'id'}, 
              {id:'author'}
              ]}" value="[]" />
    
    <script src="./json?js&var=svc"></script>
    <script>
      function go() {
        svc.getRecommendedBooks(
          {},
          function(r) {
            jmaki.getWidget('table').addRows(r);
          }
        );
      }
    </script>
    <input type="button" value="Run" onclick="go()"/>
  </body>
</html>
