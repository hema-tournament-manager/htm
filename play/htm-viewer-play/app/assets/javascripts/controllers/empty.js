"use strict";

var EmptyCtrl = function($scope, $location, playRoutes) {
	$scope.change = function() {
		$location.path("/fight");
	};
};