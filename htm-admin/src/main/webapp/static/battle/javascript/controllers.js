"use strict";

var BattleCtrl = function($rootScope, $scope, $timeout, $modal, $location, playRoutes, appService) {
	if (!$rootScope.arena) {
		$location.path("/");
	} else {
	console.log($rootScope.arena);
	$rootScope.title = "Loading...";
	$rootScope.subtitle = $rootScope.arena.name;
	var _ = window._;
	
	$scope.arena = $rootScope.arena;
    $scope.fights = new Array();
    $scope.currentFight = {globalOrder: -1, started: false};
    $scope.totalScore = {a: 0, b: 0, d: 0, x: 0};
    $scope.fightsShowing = [1, 5];
    $scope.round = false;
	
	playRoutes.controllers.AdminInterface.arena($scope.arena.id).get().success(function(data, status) {
		$rootScope.title = data[0].round.tournament.name;
		$scope.pools = data;
		var globalOrder = 1;
		_.each(_.reject($scope.pools, function(pool) { return pool.finished; }), function(pool) {
			for (var i = 1; i <= pool.fightCount; i++) {
				var fight = {poolId: pool.id, pool: pool, order: i, globalOrder: globalOrder++, loading: true, started: false};
				$scope.fights.push(fight);
				playRoutes.controllers.AdminInterface.poolFight(pool.id, fight.order).get().success(function(data, status) {
					var fight = _.findWhere($scope.fights, {poolId: data.poolId, order: data.order});
					fight = _.extend(fight, data);
					fight.loading = false;
    				fight['totalScore'] = function() {
    					return _.reduce(this.scores, function(memo, score) {
    						memo.a += score.diffA;
    						memo.b += score.diffB;
    						memo.d += score.diffDouble;
    						memo.x += score.isExchange ? 1 : 0;
    						return memo;
    					}, {a: 0, b: 0, d: 0, x: 0});
    				};
					$scope.fights[fight.globalOrder - 1] = fight;
					if (($scope.currentFight.globalOrder == -1 || fight.globalOrder < $scope.currentFight.globalOrder) && fight.timeStop == 0) {
						$scope.currentFight = fight;
					}
				});
			}
		});
	});
	
    $scope.pendingOperation = false;
    $scope.redoScore = false;
    
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
    	playRoutes.controllers.AdminInterface.timerUpdate($scope.currentFight.id).post({action: $scope.timer.running ? "start" : "stop", time: $scope.timer.currentTime});
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
    	return item.globalOrder < $scope.fightsShowing[0];
    };
    
    $scope.inRangeFunction = function(item) {
    	return item.globalOrder >= $scope.fightsShowing[0] && item.globalOrder <= $scope.fightsShowing[1];
    };
    
    $scope.afterRangeFunction = function(item) {
    	return item.globalOrder > $scope.fightsShowing[1];
    };
    
    $scope.scoreSide = "blue";
    
    $scope.scoreSelected = function(score) {};
    
    $scope.cancelScoreSelect = function () {
    	$('#score-options').hide();
    };
    
    $scope.undoClicked = function () {
    	$scope.redoScore = $scope.currentFight.scores.pop();
    	$scope.sendUpdate();
    };
    
    $scope.redoClicked = function () {
    	$scope.currentFight.scores.push($scope.redoScore);
    	$scope.redoScore = false;
    	$scope.sendUpdate();
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
    	$scope.sendUpdate();
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
    	      templateUrl: '/static/battle/templates/exchangeList.html',
    	      controller: ExchangeListCtrl,
    	      resolve: {
    	        exchanges: function () {
    	          return $scope.currentFight.scores;
    	        }
    	      }
    	    });
    };
    
    $scope.exchangeLimitReached = function() {
    	return $scope.round != false && $scope.currentFight.started && $scope.round.exchangeLimit > 0 && $scope.currentFight.totalScore().x >= $scope.round.exchangeLimit;
    };
    
    $scope.timeLimitReached = function() {
    	return $scope.round != false && $scope.currentFight.started && $scope.timerValue() >= $scope.round.timeLimitOfFight;
    };
    
    $scope.doubleHitLimitReached = function() {
    	return $scope.round != false && $scope.currentFight.started && $scope.currentFight.totalScore().d >= 3;
    };
    
    $scope.sendUpdate = function() {
    	playRoutes.controllers.AdminInterface.fightUpdate().post($scope.currentFight);
    };
    
     $scope.startFight = function() {
		$scope.resetTimer();
    	
		if ($scope.currentFight.globalOrder > -1) {
	    	playRoutes.controllers.AdminInterface.round($scope.currentFight.pool.round.id).get().success(function(data, status) {
	    		$scope.round = data;
	    		
	    		$scope.currentFight.started = true;
	    		$scope.currentFight.timeStart = Date.now();
	    		$scope.timer.currentTime = _.reduce($scope.currentFight.scores, function(memo, score) { return Math.max(score.timeInFight, memo); }, 0);
	    		$scope.timer.displayTime = $scope.timerValue();
	    		
	    		$scope.sendUpdate();
	    	});
    	}
    };
    
    $scope.findCurrentFight = function() {
    	return _.find($scope.fights, function(fight) { return fight.timeStop == 0; });
    };
    
    $scope.confirmFight = function() {
    	if ($scope.currentFight.globalOrder > -1) {
    		$scope.stopTimer();
    		$scope.currentFight.timeStop = Date.now();
    		$scope.currentFight.netDuration = $scope.timerValue();
    		
    		playRoutes.controllers.AdminInterface.fightUpdate().post($scope.currentFight).success(function(data, status) {
    			$scope.currentFight = $scope.findCurrentFight();
    		});
    	}
    };
    
    $scope.$watch('currentFight', function(newValue, oldValue) {
		$scope.fightsShowing[0] = Math.max($scope.currentFight.globalOrder - 2, 1);
    	$scope.fightsShowing[1] = Math.min($scope.fightsShowing[0] + 4, $scope.fights.length);
    	$scope.fightsShowing[0] = Math.max($scope.fightsShowing[1] - 4, 1);
    });
    
    $(document).keypress(function(event) {
    	// space
		if (event.keyCode == 32) {
			$scope.$apply(function() {
				if ($scope.currentFight.started) {
					$scope.toggleTimer();
				} else {
					$scope.startFight();
				}
			});
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
	}
};

var PoolsCtrl = function($rootScope, $scope, $timeout, $location, playRoutes, appService) {
	$rootScope.title = "HEFFAC 2013";
	$rootScope.subtitle = "";
	
	$scope.arenas = new Array();
	var _ = window._;

	playRoutes.controllers.AdminInterface.arenas().get().success(function(data, status) {
		$scope.arenas = _.map(data, function(arena) { arena.fetchedPools = new Array(); return arena; });
		_.each($scope.arenas, function(arena) {
			playRoutes.controllers.AdminInterface.arena(arena.id).get().success(function(data, status) {
				console.log(JSON.stringify(data));
				arena.fetchedPools = data;
			});
		});
	}).error(function(data, status, headers) {
		$scope.arenas = "Error " + status + ": " + headers();
	});
	
	$scope.subscribe = function(arena) {
		$rootScope.arena = _.omit(arena, "fetchedPools");
		$location.path("/fight");
	};
};
