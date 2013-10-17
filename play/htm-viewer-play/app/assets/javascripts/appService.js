'use strict';

/* Services */

// Demonstrate how to register services
// In this case it is a simple value service.
angular.module('htm.services', [])
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
			  "timer": {
				  "time": 0
			  },
			  "poolSummary": {
				  "order": 1,
				  "round": {
					  "name": "Poule Phase, Round 1",
					  "exchangeLimit": 10,
					  "tournament": {
						  "name": "Longsword Open"
					  }
				  }
			  },
			  "fighterA": {
				  "name": "Rood",
				  "country": "NL"
			  },
			  "fighterB": {
				  "name": "Blauw",
				  "country": "NL"
			  }
		  },
		  "overview/arena": {
			  "message": "",
			  "arena": "",
			  "fights": []
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
