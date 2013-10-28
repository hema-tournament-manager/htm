"use strict";

var FightCtrl = function($scope, $timeout, playRoutes, stateService) {
	_.extend($scope, stateService.get("fight"));
	$scope.totalScore = {a: 0, b: 0, d: 0};
	
	$scope.updateScore = function() {
		$scope.totalScore = _.reduce($scope.scores, function(memo, score) {
			memo.a += score.diffA;
			memo.b += score.diffB;
			memo.d += score.diffDouble;
			memo.x += score.diffExchange;
			return memo;
		}, {a: 0, b: 0, d: 0, x: 0});
	}
	
	$scope.updateScore();
	
    $scope.timerValue = function() {
    	var result = $scope.timer.time;
    	if ($scope.timer.running) {
    		result += Date.now() - $scope.timer.lastStart;
    	}
    	return result;
    };
    
    $scope.startTimer = function() {
    	$scope.timer.running = true;
		$scope.timer.lastStart = Date.now();
		$scope.tick();
    }
    $scope.stopTimer = function() {
    	$scope.timer.running = false;
		$scope.timer.displayTime = $scope.timerValue();
    }
    
    $scope.tick = function() {
    	if ($scope.timer.running) {
    		$scope.timer.displayTime = $scope.timerValue();
    		$timeout($scope.tick, 500);
    	}
    };

    if ($scope.timer.action == "start") {
    	$scope.startTimer();
    }
    else {
    	$scope.stopTimer();    	
    }
    
	stateService.change(function(view, state, update) {
		if (view == "fight") {
			_.extend($scope, state);
			if (_.has(update, "timer")) {
				console.log(update.timer.action);
				if (update.timer.action == "start") {
					$scope.startTimer();
				} else {
					$scope.stopTimer();
				}
			}
			$scope.updateScore();
		}
	});
	
	$scope.range = function(n) {
        return new Array(n);
    };
};