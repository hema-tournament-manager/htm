"use strict";

var BattleCtrl = function($scope, $timeout, playRoutes, appService) {
	var _ = window._;
	
	$scope.arena = {name: "Arena 1"};
//	$scope.tournament = {name: "Longsword Open"};
	$scope.round = {};
	$scope.poolSummary = {};
	$scope.pool = {};
    $scope.fights = new Array();
    $scope.currentFight = 1;
    $scope.fightsShowing = [1, 5];
    $scope.fight = {};
    $scope.possibleScores = [0, 1, 2, 3];
    
    playRoutes.controllers.Application.currentPool().get().success(function(data, status) {
    	$scope.poolSummary = data;
    	playRoutes.controllers.AdminInterface.pool($scope.poolSummary.id).get().success(function(data) {
    		$scope.pool = data;
    		_.each($scope.pool.fights, function(fight) {
    			playRoutes.controllers.AdminInterface.fight(fight).get().success(function(data) {
    				var fight = data;
    				fight['exchanges'] = new Array();
    				fight['totalScore'] = function() {
    					return _.reduce(this.scores, function(memo, score) {
    						memo.a += score.diffA;
    						memo.b += score.diffB;
    						memo.d += score.diffDouble;
    						return memo;
    					}, {a: 0, b: 0, d: 0});
    				};
    				$scope.fights.push(fight);
    				$scope.fight = _.find($scope.fights, function(f) { return f.order == $scope.currentFight; })
    			});
    		});
    	});
    });
    playRoutes.controllers.Application.currentRound().get().success(function(data, status) {
    	$scope.round = data;
    });
    
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
    	return item.order < $scope.fightsShowing[0];
    };
    
    $scope.inRangeFunction = function(item) {
    	return item.order >= $scope.fightsShowing[0] && item.order <= $scope.fightsShowing[1];
    };
    
    $scope.afterRangeFunction = function(item) {
    	return item.order > $scope.fightsShowing[1];
    };
    
    $scope.incCurrentFight = function() {
    	$scope.currentFight = $scope.currentFight + 1;
    	$scope.fightsShowing[0] = Math.max($scope.currentFight - 2, 1);
    	$scope.fightsShowing[1] = Math.min($scope.fightsShowing[0] + 4, $scope.fights.length);
    	$scope.fightsShowing[0] = Math.max($scope.fightsShowing[1] - 4, 1);
    };
    
    $scope.scoreSide = "blue";
    
    $scope.scoreSelected = function(score) {};
    
    $scope.cancelScoreSelect = function () {
    	$('#score-options').hide();
    };
    
    $scope.hitButtonClicked = function(scoreType, side) {
    	$scope.scoreSide = side;
    	if (scoreType == "clean") {
	    	$scope.scoreSelected = function(score) {
	    		$scope.fight.exchanges.push({
	    			time: $scope.timer.currentTime, 
	    			a: side == "red" ? score : 0, 
	    			b: side == "blue" ? score : 0, 
	    			type: scoreType, 
	    			d: 0});
	    		$scope.fight.score[side == "red" ? "a" : "b"] += score;
	    		$('#score-options').hide();
	    	}
    	} else {
    		$scope.scoreSelected = function(score) {
    			var firstScore = score;
    			$scope.scoreSide = side == "red" ? "blue" : "red";
    			$scope.scoreSelected = function(score) {
    				$scope.fight.exchanges.push({
    	    			time: $scope.timer.currentTime, 
    	    			a: side == "red" ? firstScore : score, 
    	    			b: side == "blue" ? firstScore : score, 
    	    			type: scoreType, 
    	    			d: 0});
    	    		$scope.fight.score.a += side == "red" ? firstScore : score;
    	    		$scope.fight.score.b += side == "blue" ? firstScore : score;
    	    		$('#score-options').hide();
    			}
    		}
    	}
    	$('#score-options').show().position({my: "center center", at: "center center", of: "#" + scoreType + "-" + side + "-btn"});
    };
    
    $scope.doubleHitClicked = function() {
    	$scope.fight.exchanges.push({
			time: $scope.timer.currentTime, 
			a: 0, 
			b: 0, 
			type: "double", 
			d: 1});
    	$scope.fight.score.d += 1;
    };
    
    $scope.noHitClicked = function() {
    	$scope.fight.exchanges.push({
			time: $scope.timer.currentTime, 
			a: 0, 
			b: 0, 
			type: "none", 
			d: 0});
    };
    
    $scope.exchangeLimitReached = function() {
    	return $scope.fight.exchanges.length >= 10;
    };
    
    $scope.timeLimitReached = function() {
    	return $scope.timer.currentTime >= $scope.round.timeLimit;
    };
    
    $scope.doubleHitLimitReached = function() {
    	return $scope.fight.totalScore().d >= 3;
    };
    
    $(document).keypress(function(event) {
		// space
		if (event.keyCode == 32) {
			$scope.toggleTimer();
		}
	});
    
    $('#score-options').hide();
	
};


var PoolsCtrl = function($scope, $timeout, playRoutes, appService) {
	$scope.tournaments = new Array();
	$scope.currentPoolId = -1;
	var _ = window._;

	playRoutes.controllers.AdminInterface.tournaments().get().success(function(data, status) {
		$scope.tournaments = _.map(data, function(tournament) { tournament.fetchedRounds = new Array(); return tournament; });
		_.each($scope.tournaments, function(tournament) {
			_.each(tournament.rounds, function(roundId) {
				playRoutes.controllers.AdminInterface.round(roundId).get().success(function(data, status) {
					tournament.fetchedRounds.push(data);
				});
			});
		});
	}).error(function(data, status, headers) {
		$scope.tournaments = "Error " + status + ": " + headers();
	});
	
	$scope.refreshCurrentPoolId = function() {
		playRoutes.controllers.Application.currentPool().get().success(function(data, status) {
			$scope.currentPoolId = data;
		});
	};
	
	$scope.subscribe = function(pool) {
		playRoutes.controllers.Application.subscribe().post(pool).success(function(data, status) {
			window.location = playRoutes.controllers.Application.fight().url;
		});
	};
	
	$scope.refreshCurrentPoolId();
};
