"use strict";

var ImageCtrl = function($scope, stateService) {
	$scope.resolution = resolution;
	$scope.message = stateService.get("image").message;
	$scope.image = stateService.get("image").image;
	
	stateService.change(function(view, state) {
		if (view == "image") {
			$scope.message = state.message;
			$scope.image = state.image;
		}
	});
};