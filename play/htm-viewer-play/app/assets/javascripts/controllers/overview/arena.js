"use strict";

var OverviewArenaCtrl = function($scope, $timeout, stateService) {
	var _ = window._;
	_.extend($scope, stateService.get("overview/arena"));
	$scope.displayedFights = $scope.fights;
	$scope.currentFight = $scope.displayedFights.shift();
	$scope.fightBuffer = false;

	$scope.currentTime = Date.now();
	
	$scope.tick = function() {
		$scope.currentTime = Date.now();
		$timeout($scope.tick, 1000);
	};
	$scope.tick();
	
	// cycle the fights
	var nextFightTimeout = false;
	$scope.nextFight = function() {
		if ($scope.displayedFights.length > 2) {
			if ($scope.fightBuffer) {
				$scope.displayedFights.push($scope.fightBuffer);
			}
			$scope.fightBuffer = $scope.displayedFights.shift();
		}
		nextFightTimeout = $timeout($scope.nextFight, 10000);
	}
	nextFightTimeout = $timeout($scope.nextFight, 10000);
	
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
		if (nextFightTimeout) {
			$timeout.cancel(nextFightTimeout);
		}

		if (view == "overview/arena") {
			_.extend($scope, state);
			$scope.displayedFights = state.fights;
			$scope.currentFight = $scope.displayedFights.shift();
			$scope.fightBuffer = false;
			nextFightTimeout = $timeout($scope.nextFight, 10000);
		}
	});
};