"use strict";

var OverviewArenaCtrl = function($scope, $timeout, stateService) {
	var _ = window._;
	$scope.message = stateService.get("overview/arena").message;
	$scope.arena = stateService.get("overview/arena").arena;
	$scope.displayedPools = [];
	$scope.poolBuffer = false;

	$scope.currentTime = Date.now();
	
	$scope.tick = function() {
		$scope.currentTime = Date.now();
		$timeout($scope.tick, 1000);
	};
	$scope.tick();
	
	var nextPoolTimeout = false;
	$scope.nextPool = function() {
		if ($scope.displayedPools.length > 1) {
			if ($scope.poolBuffer) {
				$scope.displayedPools.push($scope.poolBuffer);
			}
			$scope.poolBuffer = $scope.displayedPools.shift();
		}
		nextPoolTimeout = $timeout($scope.nextPool, 10000);
	}
	
	$scope.totalScore = function(fight) {
		return _.reduce(fight.scores, function(memo, score) {
			memo.a += score.diffA;
			memo.b += score.diffB;
			memo.d += score.diffDouble;
			memo.x += score.isExchange ? 1 : 0;
			return memo;
		}, {a: 0, b: 0, d: 0, x: 0});
	};
	
	stateService.change(function(view, state) {
		if (nextPoolTimeout) {
			$timeout.cancel(nextPoolTimeout);
		}

		if (view == "overview/arena") {
			$scope.message = state.message;
			$scope.arena = state.arena;
			$scope.displayedPools = state.pools;
			$scope.currentPool = $scope.displayedPools.shift();
			$scope.poolBuffer = false;
			nextPoolTimeout = $timeout($scope.nextPool, 10000);
		}
	});
};