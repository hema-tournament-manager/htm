"use strict";

var ControllerCtrl = function($rootScope, $scope, $timeout, $modal, $location, playRoutes) {
	$scope.viewers = new Array();
	$scope.arenas = false;
	
	$scope.hasViewers = function() {
		return _.findWhere($scope.viewers, {selected: true}) != undefined;
	};
	
	playRoutes.controllers.AdminInterface.viewers().get().success(function(data, status) {
		$scope.viewers = data;
	});
	
	playRoutes.controllers.AdminInterface.arenas().get().success(function(data, status) {
		$scope.arenas = data;
	});
	
	$scope.update = function(view, payload) {
		var viewers = _.pluck(_.where($scope.viewers, {selected: true}), 'id');
		var data = {"view": view, "viewers": viewers, "payload": payload};
		console.log("Sending " + JSON.stringify(data));
		playRoutes.controllers.AdminInterface.viewerUpdate().post(data);
	};
};
