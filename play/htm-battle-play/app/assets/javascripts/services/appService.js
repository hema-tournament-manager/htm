'use strict';

/* Services */


// Demonstrate how to register services
// In this case it is a simple value service.
angular.module('htm.services', []).
  factory("appService", ["$http", "$q", "playRoutes", function($http, $q, playRoutes) {
    var AppService = function() {
      var self = this;
      self.defaultInstallationFolder = function() {
        return playRoutes.controllers.Application.defaultInstallationFolder().get().then(function (response) {
        	return response.data;
        });
      };
      self.executeInstallation = function(path, apps) {
    	return playRoutes.controllers.Application.install().post({path: path, apps: apps});
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
  }]);
