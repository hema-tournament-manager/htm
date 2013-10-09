"use strict";

var BattleCtrl = function($scope, $timeout, appService) {
	var _ = window._;
	
	$scope.arena = {name: "Arena 1"};
	$scope.tournament = {name: "Longsword Open"};
	$scope.round = {name: "Round 1"};
	$scope.pool = {order: 3};
    $scope.fights = appService.generateFights(20);
    $scope.currentFight = 1;
    $scope.fightsShowing = [1, 5];
    $scope.fight = _.find($scope.fights, function(f) { return f.index == $scope.currentFight; });
    
    $scope.timer = {'running': false, 'lastStart': -1, 'currentTime': 0};
    
    $scope.toggleTimer = function() {
    	$scope.timer.running = !$scope.timer.running;
    	if ($scope.timer.running) {
    		$timeout($scope.tick, 1000);
    	}
    }
    $scope.tick = function() {
    	if ($scope.timer.running) {
    		$scope.timer.currentTime = $scope.timer.currentTime + 1;
    		$timeout($scope.tick, 1000);
    	}
    };
    
    $scope.fightsBefore = function() {
    	return $scope.fightsShowing[0] > 1;
    };
    
    $scope.fightsAfter = function() {
    	return $scope.fightsShowing[1] < $scope.fights.length;
    };
    
    $scope.beforeRangeFunction = function(item) {
    	return item.index < $scope.fightsShowing[0];
    };
    
    $scope.inRangeFunction = function(item) {
    	return item.index >= $scope.fightsShowing[0] && item.index <= $scope.fightsShowing[1];
    };
    
    $scope.afterRangeFunction = function(item) {
    	return item.index > $scope.fightsShowing[1];
    };
    
    $scope.incCurrentFight = function() {
    	$scope.currentFight = $scope.currentFight + 1;
    	$scope.fightsShowing[0] = Math.max($scope.currentFight - 2, 1);
    	$scope.fightsShowing[1] = Math.min($scope.fightsShowing[0] + 4, $scope.fights.length);
    	$scope.fightsShowing[0] = Math.max($scope.fightsShowing[1] - 4, 1);
    };
    
    $(document).keypress(function(event) {
		// space
		if (event.keyCode == 32) {
			$scope.toggleTimer();
		}
	});
	
};
