"use strict";

var OverviewArenaCtrl = function($scope, stateService) {
	$scope.message = stateService.get("overview/arena").message;
	$scope.arena = stateService.get("overview/arena").arena;
	$scope.fights = stateService.get("overview/arena").fights;
	
	stateService.change(function(view, state) {
		if (view == "overview/arena") {
			$scope.message = state.message;
			$scope.arena = state.arena;
			$scope.fights = state.fights;
		}
	});
};