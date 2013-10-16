"use strict";

var FightCtrl = function($scope, playRoutes, stateService) {
	_.extend($scope, stateService.get("fight"));
	$scope.totalScore = {a: 0, b: 0, d: 0};
	
	$scope.updateScore = function() {
		$scope.totalScore = {a: 0, b: 0, d: 0};
	}
	
	$scope.updateScore();
	
	stateService.change(function(view, state) {
		if (view == "fight") {
			_.extend($scope, state);
			$scope.updateScore();
		}
	});
	
	$scope.range = function(n) {
        return new Array(n);
    };
};