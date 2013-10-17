"use strict";

var ControllerCtrl = function($rootScope, $scope, $timeout, $modal, $location, playRoutes) {
	$scope.viewers = new Array();
	
	playRoutes.controllers.AdminInterface.viewers().get().success(function(data, status) {
		$scope.viewers = data;
	});
	
	$scope.update = function(view, payload) {
		var viewers = _.pluck(_.where($scope.viewers, {selected: true}), 'id');
		var data = {"view": view, "viewers": viewers, "payload": payload};
		playRoutes.controllers.AdminInterface.viewerUpdate().post();
	};
};
