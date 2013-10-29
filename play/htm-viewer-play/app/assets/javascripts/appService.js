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
		  "image": {
			  "message": "",
			  "image": "does_not_exist.jpg"
		  },
		  "fight": {
			  "message": "",
			  "timer": {
				  "time": 0,
				  "displayTime": 0
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
				  "country": "",
				  "club": "Academie voor Middeleeuwse Krijgskunst"
			  },
			  "fighterB": {
				  "name": "Blauw",
				  "country": "NL",
				  "club": "Stockholmspolisens Idrottsförening Fäktning"
			  }
		  },
		  "overview/arena": {
			  "message": "",
			  "arena": "",
			  "pools": []
		  },
		  "participant/footer": {
			  "participant": {
				  "name": "Een of andere Deelnemer",
				  "club": "Een Of Andere Vereniging",
				  "country": "SE"
			  }
		  }
	  };
	  
	  $.getJSON("/initialState").done(function( json ) {
		  state = json;
	  })
	  
	  return {
		  put: function(view_, update) {
			  view = view_;
			  console.log(view_);
			  console.log(state[view]);
			  state[view] = _.extend(state[view], update);
			  callback(view, state[view], update);
		  },
		  broadcast: function(update) {
			  state[view] = _.extend(state[view], update);
			  callback(view, state[view], update);
		  },
		  get: function(view) {
			  return state[view];
		  },
		  change: function(callback_) {
			  callback = callback_;
		  }
	  };
  });
