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
    };
    return new AppService();
  }]);
