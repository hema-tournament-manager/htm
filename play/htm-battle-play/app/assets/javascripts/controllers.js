"use strict";

var BattleCtrl = function($scope, $timeout, appService) {
    $scope.fight = {'a': 'Fighter A', 'b': 'Fighter B'};
    $scope.fights = appService.generateFights(20);
    $scope.currentFight = 1;
    $scope.timer = {'running': false, 'lastStart': -1, 'currentTime': 0};
    var _ = window._;
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
    	return currentFight > 3;
    };
    
    $scope.beforeRangeFunction = function(item) {
    	return item.index < $scope.currentFight - 2;
    };
    
    $scope.inRangeFunction = function(item) {
    	return Math.abs(item.index - $scope.currentFight) < 3;
    };
    
    $scope.afterRangeFunction = function(item) {
    	return item.index > $scope.currentFight + 2;
    };
    
    $scope.incCurrentFight = function() {
    	$scope.currentFight = $scope.currentFight + 1;
    };
    
  };
