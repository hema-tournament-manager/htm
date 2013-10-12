"use strict";

var BattleCtrl = function($scope, $timeout, $modal, playRoutes, appService) {
	var _ = window._;
	
	$scope.arena = {name: "Arena 1"};
//	$scope.tournament = {name: "Longsword Open"};
	$scope.round = {};
	$scope.poolSummary = {};
	$scope.pool = {};
    $scope.fights = new Array();
    $scope.currentFight = {order: -1};
    $scope.fightsShowing = [1, 5];
    $scope.possibleScores = [0, 1, 2, 3];
    $scope.pendingOperation = false;
    
    playRoutes.controllers.Application.currentPool().get().success(function(data, status) {
    	$scope.poolSummary = data;
    	playRoutes.controllers.AdminInterface.pool($scope.poolSummary.id).get().success(function(data) {
    		$scope.pool = data;
    		_.each($scope.pool.fights, function(fight) {
    			playRoutes.controllers.AdminInterface.fight(fight).get().success(function(data) {
    				var fight = data;
    				fight['totalScore'] = function() {
    					return _.reduce(this.scores, function(memo, score) {
    						memo.a += score.diffA;
    						memo.b += score.diffB;
    						memo.d += score.diffDouble;
    						memo.x += score.isExchange ? 1 : 0;
    						return memo;
    					}, {a: 0, b: 0, d: 0, x: 0});
    				};
    				$scope.fights.push(fight);
    			});
    		});
    	});
    });
    playRoutes.controllers.Application.currentRound().get().success(function(data, status) {
    	$scope.round = data;
    });
    
    $scope.timer = {running: false, lastStart: -1, currentTime: 0, displayTime: 0};
    
    $scope.timerValue = function() {
    	var result = $scope.timer.currentTime;
    	if ($scope.timer.running) {
    		result += Date.now() - $scope.timer.lastStart;
    	}
    	return result;
    };
    
    $scope.toggleTimer = function() {
    	$scope.timer.running = !$scope.timer.running;
    	if ($scope.timer.running) {
    		$scope.timer.lastStart = Date.now();
    		$scope.tick();
    	} else {
    		$scope.timer.currentTime += Date.now() - $scope.timer.lastStart;
    		$scope.timer.displayTime = $scope.timer.currentTime;
    	}
    }
    $scope.tick = function() {
    	if ($scope.timer.running) {
    		$scope.timer.displayTime = $scope.timerValue();
    		$timeout($scope.tick, 500);
    	}
    };
    
    $scope.stopTimer = function() {
    	if ($scope.timer.running) {
    		$scope.toggleTimer();
    	}
    };
    
    $scope.resetTimer = function() {
    	$scope.stopTimer();
    	$scope.timer.currentTime = 0;
    	$scope.timer.displayTime = 0;
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
    
    $scope.scoreSide = "blue";
    
    $scope.scoreSelected = function(score) {};
    
    $scope.cancelScoreSelect = function () {
    	$('#score-options').hide();
    };
    
    
    $scope.pushExchange = function(exchange) {
    	$scope.currentFight.scores.push({
    		timeInFight: exchange.time,
    		timeInWorld: Date.now(),
    		diffA: exchange.a,
    		diffB: exchange.b,
    		diffAAfterblow: 0,
    		diffBAfterblow: 0,
    		diffDouble: exchange.d,
    		scoreType: exchange.type,
    		isSpecial: false,
    		isExchange: true
    	});
    	playRoutes.controllers.Application.fightUpdate().post($scope.currentFight);
    };
    
    $scope.hitButtonClicked = function(scoreType, side) {
    	$scope.scoreSide = side;
    	if (scoreType == "clean") {
	    	$scope.scoreSelected = function(score) {
	    		$scope.pushExchange({
		    			time: $scope.timerValue(),
		    			a: side == "red" ? score : 0, 
		    			b: side == "blue" ? score : 0, 
		    			type: scoreType, 
		    			d: 0});
	    		$('#score-options').hide();
	    	}
    	} else {
    		$scope.scoreSelected = function(score) {
    			var firstScore = score;
    			$scope.scoreSide = side == "red" ? "blue" : "red";
    			$scope.scoreSelected = function(score) {
    				$scope.pushExchange({
    	    			time: $scope.timerValue(), 
    	    			a: side == "red" ? firstScore : score, 
    	    			b: side == "blue" ? firstScore : score, 
    	    			type: scoreType, 
    	    			d: 0});
    	    		$('#score-options').hide();
    			}
    		}
    	}
    	$('#score-options').show().position({my: "center center", at: "center center", of: "#" + scoreType + "-" + side + "-btn"});
    };
    
    $scope.doubleHitClicked = function() {
    	$scope.pushExchange({
			time: $scope.timerValue(), 
			a: 0, 
			b: 0, 
			type: "double", 
			d: 1});
    };
    
    $scope.noHitClicked = function() {
    	$scope.pushExchange({
			time: $scope.timerValue(), 
			a: 0, 
			b: 0, 
			type: "none", 
			d: 0});
    };

    $scope.showExchanges = function() {
    	var modalInstance = $modal.open({
    	      templateUrl: 'exchangeList.html',
    	      controller: ExchangeListCtrl,
    	      resolve: {
    	        exchanges: function () {
    	          return $scope.currentFight.scores;
    	        }
    	      }
    	    });
    };
    
    $scope.exchangeLimitReached = function() {
    	return $scope.currentFight.order > -1 && $scope.currentFight.totalScore().x >= 10;
    };
    
    $scope.timeLimitReached = function() {
    	return $scope.currentFight.order > -1 && $scope.timerValue() >= $scope.round.timeLimitOfFight;
    };
    
    $scope.doubleHitLimitReached = function() {
    	return $scope.currentFight.order > -1 && $scope.currentFight.totalScore().d >= 3;
    };
    
    $scope.startNextFight = function() {
		$scope.resetTimer();
    	if ($scope.fights.length > 0) {
    		$scope.currentFight = _.reduce($scope.fights, function(memo, fight) {
    			if (fight.timeStop == 0 && (memo.order == -1 || fight.order < memo.order)) {
    				return fight;
    			} else {
    				return memo;
    			}
    		}, {order: -1});
    		
    		$scope.currentFight.timeStart = Date.now();
    		
    		$scope.fightsShowing[0] = Math.max($scope.currentFight.order - 2, 1);
        	$scope.fightsShowing[1] = Math.min($scope.fightsShowing[0] + 4, $scope.fights.length);
        	$scope.fightsShowing[0] = Math.max($scope.fightsShowing[1] - 4, 1);
    	}
    };
    
    $scope.confirmFight = function() {
    	if ($scope.currentFight.order > -1) {
    		$scope.stopTimer();
    		$scope.currentFight.timeStop = Date.now();
    		$scope.currentFight.netDuration = $scope.timerValue();
    		
    		playRoutes.controllers.Application.fightUpdate().post($scope.currentFight).success(function(data, status) {
    			$scope.startNextFight();
    		});
    	}
    };
    
    $(document).keypress(function(event) {
		// space
		if (event.keyCode == 32) {
			$scope.toggleTimer();
			event.preventDefault();
		}
	});
    
    $(window).on("beforeunload", function() {
    	return "You might lose some information by doing this!";
    });
    
    $('#score-options').hide();
	
};

var ExchangeListCtrl = function($scope, $modalInstance, exchanges) {
	$scope.exchanges = exchanges;
	var exchangeCounter = 1;
	_.each($scope.exchanges, function(score) {
		if (score.isExchange) {
			score.exchangeId = exchangeCounter++;
		} else {
			score.exchangeId = "-";
		}
	});
	
	$scope.close = function() {
		$modalInstance.dismiss('cancel');
	};
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
