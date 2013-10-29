var jsRoutes = {}; (function(_root){
var _nS = function(c,f,b){var e=c.split(f||"."),g=b||_root,d,a;for(d=0,a=e.length;d<a;d++){g=g[e[d]]=g[e[d]]||{}}return g}
var _qS = function(items){var qs = ''; for(var i=0;i<items.length;i++) {if(items[i]) qs += (qs ? '&' : '') + items[i]}; return qs ? ('?' + qs) : ''}
var _s = function(p,s){return p+((s===true||(s&&s.secure))?'s':'')+'://'}
var _wA = function(r){return {ajax:function(c){c=c||{};c.url=r.url;c.type=r.method;return jQuery.ajax(c)}, method:r.method,type:r.method,url:r.url,absoluteURL: function(s){return _s('http',s)+'localhost:9000'+r.url},webSocketURL: function(s){return _s('ws',s)+'localhost:9000'+r.url}}}
_nS('controllers.AdminInterface'); _root.controllers.AdminInterface.arena = 
      function(id) {
      return _wA({method:"GET", url:"/" + "api/arena/" + (function(k,v) {return v})("id", id)})
      }
   
_nS('controllers.AdminInterface'); _root.controllers.AdminInterface.arenas = 
      function() {
      return _wA({method:"GET", url:"/" + "api/arenas"})
      }
   
_nS('controllers.AdminInterface'); _root.controllers.AdminInterface.fight = 
      function(id) {
      return _wA({method:"GET", url:"/" + "api/fight/" + (function(k,v) {return v})("id", id)})
      }
   
_nS('controllers.AdminInterface'); _root.controllers.AdminInterface.fightUpdate = 
      function() {
      return _wA({method:"POST", url:"/" + "api/fight/update"})
      }

_nS('controllers.AdminInterface'); _root.controllers.AdminInterface.timerUpdate = 
    function(id) {
    return _wA({method:"POST", url:"/" + "api/fight/update/" + (function(k,v) {return v})("id", id) + "/timer"})
    }

_nS('controllers.AdminInterface'); _root.controllers.AdminInterface.messageUpdate = 
    function(id) {
    return _wA({method:"POST", url:"/" + "api/fight/update/" + (function(k,v) {return v})("id", id) + "/message"})
    }
   
_nS('controllers.AdminInterface'); _root.controllers.AdminInterface.pool = 
      function(id) {
      return _wA({method:"GET", url:"/" + "api/pool/" + (function(k,v) {return v})("id", id)})
      }
   
_nS('controllers.AdminInterface'); _root.controllers.AdminInterface.poolFight = 
      function(id,order) {
      return _wA({method:"GET", url:"/" + "api/pool/" + (function(k,v) {return v})("id", id) + "/fight/" + (function(k,v) {return v})("order", order)})
      }
   
_nS('controllers.AdminInterface'); _root.controllers.AdminInterface.round = 
      function(id) {
      return _wA({method:"GET", url:"/" + "api/round/" + (function(k,v) {return v})("id", id)})
      }
   
_nS('controllers.AdminInterface'); _root.controllers.AdminInterface.tournaments = 
    function() {
    return _wA({method:"GET", url:"/" + "api/tournaments"})
    }
 
_nS('controllers.AdminInterface'); _root.controllers.AdminInterface.viewers = 
    function() {
    return _wA({method:"GET", url:"/" + "api/viewers"})
    }
 
_nS('controllers.AdminInterface'); _root.controllers.AdminInterface.viewerUpdate = 
    function() {
    return _wA({method:"POST", url:"/" + "api/viewer/update"})
    }
 
_nS('controllers.AdminInterface'); _root.controllers.AdminInterface.images = 
    function() {
    return _wA({method:"GET", url:"/" + "api/images"})
    }

_nS('controllers.AdminInterface'); _root.controllers.AdminInterface.participants = 
    function() {
    return _wA({method:"GET", url:"/" + "api/participants"})
    }

_nS('controllers.AdminInterface'); _root.controllers.AdminInterface.countries = 
    function() {
    return _wA({method:"GET", url:"/" + "api/countries"})
    }
 
})(jsRoutes)
          