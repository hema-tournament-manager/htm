"use strict";

var EmptyCtrl = function($scope, $location, playRoutes, stateService) {
	$scope.message = stateService.get("empty").message;
	
	stateService.change(function(view, state) {
		if (view == "empty") {
			$scope.message = state.message;
		}
	});
};