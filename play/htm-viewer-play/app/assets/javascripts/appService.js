'use strict';

/* Services */

// Demonstrate how to register services
// In this case it is a simple value service.
angular.module('htm.services', []).
  factory("appService", ["$http", "$q", "playRoutes", function($http, $q, playRoutes) {
    var AppService = function() {
      var self = this;
      self.fetchTournaments = function(adminBase) {
    	  return $http.get(adminBase + "/api/tournaments");
      };
      
      self.generateFights = function(n) {
    	  var result = Array();
    	  for (var i = 1; i <= n; i++) {
    		  var fight = {
        			  id: i, 
        			  index: i, 
        			  plannedTime: "14:30", 
        			  a: {
        				  name: "A" + i 
        			  },
        			  b: {
        				  name: "B" + i
        			  },
        			  score: {a: 0, d: 0, b: 0},
        			  exchanges: new Array()
        		  };
    		  result.push(fight);
    	  }
    	  return result;
      }
    };
    return new AppService();
  }])
  .factory("stateService", function() {
	  var _ = window._;
	  var callback = function(update) {};
	  var view = "empty";
	  var state = {
		  "empty": {
			  "message": ""
		  },
		  "fight": {
			  "message": "",
			  "timer": {},
			  "fighterA": {
				  "name": "Rood",
				  "country": "NL"
			  },
			  "fighterB": {
				  "name": "Blauw",
				  "country": "NL"
			  }
		  }
	  };
	  return {
		  put: function(view_, update) {
			  view = view_;
			  console.log(view_);
			  console.log(state[view]);
			  state[view] = _.extend(state[view], update);
			  callback(view, state[view]);
		  },
		  get: function(view) {
			  return state[view];
		  },
		  change: function(callback_) {
			  callback = callback_;
		  }
	  };
  });
