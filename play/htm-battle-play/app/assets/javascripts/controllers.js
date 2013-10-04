"use strict";

var BattleCtrl = function($scope, $timeout, appService) {
    $scope.fight = {'a': 'Fighter A', 'b': 'Fighter B'};
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
  };
