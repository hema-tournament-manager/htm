"use strict";

var OverviewArenaCtrl = function($scope, $timeout, stateService) {
	var _ = window._;
	_.extend($scope, stateService.get("overview/arena"));
	$scope.displayedPools = $scope.pools;
	$scope.currentPool = $scope.displayedPools.shift();
	$scope.poolBuffer = false;

	$scope.currentTime = Date.now();
	
	$scope.tick = function() {
		$scope.currentTime = Date.now();
		$timeout($scope.tick, 1000);
	};
	$scope.tick();
	
	var nextPoolTimeout = false;
	$scope.nextPool = function() {
		if ($scope.displayedPools.length > 2) {
			if ($scope.poolBuffer) {
				$scope.displayedPools.push($scope.poolBuffer);
			}
			$scope.poolBuffer = $scope.displayedPools.shift();
		}
		nextPoolTimeout = $timeout($scope.nextPool, 10000);
	}
	nextPoolTimeout = $timeout($scope.nextPool, 10000);
	
	$scope.totalScore = function(fight) {
		return _.reduce(fight.scores, function(memo, score) {
			memo.a += score.pointsRed;
			memo.b += score.pointsBlue;
			memo.d += score.doubles;
			memo.x += score.exchanges;
			return memo;
		}, {a: 0, b: 0, d: 0, x: 0});
	};
	
	stateService.change(function(view, state) {
		if (nextPoolTimeout) {
			$timeout.cancel(nextPoolTimeout);
		}

		if (view == "overview/arena") {
			_.extend($scope, state);
			$scope.displayedPools = state.pools;
			$scope.currentPool = $scope.displayedPools.shift();
			$scope.poolBuffer = false;
			nextPoolTimeout = $timeout($scope.nextPool, 10000);
		}
	});
};