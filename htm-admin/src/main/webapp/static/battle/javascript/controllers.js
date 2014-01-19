"use strict";

var BattleCtrl = function($rootScope, $scope, $timeout, $modal, $location, $filter, playRoutes, appService) {
	if (!$rootScope.arena) {
		$location.path("/");
	} else {
	$rootScope.title = "Loading...";
	$rootScope.subtitle = $rootScope.arena.name;
	var _ = window._;
	
	$scope.arena = $rootScope.arena;
    $scope.fights = new Array();
    $scope.currentFight = {globalOrder: -1, started: false};
    $scope.totalScore = {a: 0, b: 0, d: 0, x: 0};
    $scope.fightsShowing = [1, 5];
    $scope.announcement = "";
    $scope.announcementBuffer = "";
	
	console.log(JSON.stringify($scope.arena));
	playRoutes.controllers.AdminInterface.arena($scope.arena.id).get().success(function(data, status) {
		$rootScope.title = data[0].fight.tournament.name;
		$scope.fights = new Array();
		var globalOrder = 1;
		_.each(_.reject(data, function(fight) { return fight.fight.finished; }), function(f) {
			var fight = {globalOrder: globalOrder++, started: false, time: f.time};
			fight = _.extend(fight, f.fight);
			if (fight.fighterA.participant) {
				fight.fighterA.name = fight.fighterA.participant.shortName;
			} else {
				fight.fighterA.name = fight.fighterA.label;
			}
			if (fight.fighterB.participant) {
				fight.fighterB.name = fight.fighterB.participant.shortName;
			} else {
				fight.fighterB.name = fight.fighterB.label;
			}
			fight.name
			fight['totalScore'] = function() {
				return _.reduce(this.scores, function(memo, score) {
					memo.a += score.pointsRed;
					memo.b += score.pointsBlue;
					memo.d += score.doubles;
					memo.x += score.exchanges;
					return memo;
				}, {a: 0, b: 0, d: 0, x: 0});
			};
			fight['lastScore'] = function() {
				var result = _.reduceRight(this.scores, function(memo, score) {
					if (!memo.score) {
						if (score.scoreType == "undo") {
							memo.undos += 1;
						} else if (memo.undos == 0) {
							memo.score = score;
						} else {
							memo.undos -= 1;
						}
					}
					return memo;
				}, {undos: 0, score: false});
				return result.score;
			};
			$scope.fights.push(fight);
			if (($scope.currentFight.globalOrder == -1 || fight.globalOrder < $scope.currentFight.globalOrder) && fight.timeStop == 0) {
				$scope.currentFight = fight;
			}
		});
		console.log(JSON.stringify($scope.fights));
	});
	
	
    $scope.pendingOperation = false;
    
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
    	playRoutes.controllers.AdminInterface.timerUpdate($scope.currentFight.phaseType, $scope.currentFight.id).post({action: $scope.timer.running ? "start" : "stop", time: $scope.timer.currentTime});
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
    
    $scope.setPresent = function(participant) {
    	playRoutes.controllers.AdminInterface.setPresent(participant.id).post(true).success(function(data) {
    		if (data) {
    			_.each($scope.fights, function(fight) {
    				if (fight.fighterA.participant.id == participant.id) {
    					fight.fighterA.participant.isPresent = true;
    				} else if (fight.fighterB.participant.id == participant.id) {
    					fight.fighterB.participant.isPresent = true;
    				}
    			});
    		}
    	});
    };
    
    $scope.setGearChecked = function(tournamentId, participant) {
    	playRoutes.controllers.AdminInterface.setGearCheck(tournamentId, participant.id).post(true).success(function(data) {
    		if (data) {
    			_.each($scope.fights, function(fight) {
    				if (fight.fighterA.participant.id == participant.id) {
    					fight.fighterA.participant.gearChecked = true;
    				} else if (fight.fighterB.participant.id == participant.id) {
    					fight.fighterB.participant.gearChecked = true;
    				}
    			});
    		}
    	});
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
    
    $scope.invertScore = function(score) {
    	console.log("Inverting: " + JSON.stringify(score));
    	return {
    		timeInFight: $scope.timerValue(),
    		timeInWorld: Date.now(),
    		pointsRed: -score.pointsRed,
    		pointsBlue: -score.pointsBlue,
    		afterblowsRed: -score.afterblowsRed,
    		afterblowsBlue: -score.afterblowsBlue,
    		cleanHitsRed: -score.cleanHitsRed,
    		cleanHitsBlue: -score.cleanHitsBlue,
    		doubles: -score.doubles,
    		exchanges: -score.exchanges 
    	};
    };
    
    $scope.undoClicked = function () {
    	var lastScore = $scope.currentFight.lastScore();
    	if (lastScore) {
    		$scope.currentFight.scores.push(_.extend($scope.invertScore(lastScore), {scoreType: "undo"}));
    		$scope.sendUpdate();
    	}
    };
    
    $scope.redoClicked = function () {
    	var lastScore = _.last($scope.currentFight.scores);
    	if (lastScore.scoreType == "undo") {
    		$scope.currentFight.scores.push(_.extend($scope.invertScore(lastScore), {scoreType: "redo"}));
    		$scope.sendUpdate();
    	}
    };
    
    $scope.pushExchange = function(exchange) {
    	$scope.currentFight.scores.push({
    		timeInFight: exchange.time,
    		timeInWorld: Date.now(),
    		pointsRed: exchange.a,
    		pointsBlue: exchange.b,
    		afterblowsRed: exchange.aA,
    		afterblowsBlue: exchange.aB,
    		cleanHitsRed: exchange.cA,
    		cleanHitsBlue: exchange.cB,
    		doubles: exchange.d,
    		scoreType: exchange.type,
    		exchanges: 1
    	});
    	$scope.sendUpdate();
    };
    
    $scope.pushCorrection = function(correction) {
    	$scope.currentFight.scores.push({
    		timeInFight: correction.time,
    		timeInWorld: Date.now(),
    		pointsRed: correction.a,
    		pointsBlue: correction.b,
    		afterblowsRed: 0,
    		afterblowsBlue: 0,
    		cleanHitsRed: 0,
    		cleanHitsBlue: 0,
    		doubles: correction.d,
    		scoreType: 'correction',
    		exchanges: 0
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
		    			cA: side == "red" ? 1 : 0,
		    			cB: side == "blue" ? 1 : 0,
		    			aA: 0,
		    			aB: 0,
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
    	    			cA: 0,
    	    			cB: 0,
    	    			aA: side == "red" ? 1 : 0,
    	    			aB: side == "blue" ? 1 : 0,
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
			cA: 0,
			cB: 0,
			aA: 0,
			aB: 0,
			type: "double", 
			d: 1});
    };
    
    $scope.noHitClicked = function() {
    	$scope.pushExchange({
			time: $scope.timerValue(), 
			a: 0, 
			b: 0, 
			cA: 0,
			cB: 0,
			aA: 0,
			aB: 0,
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
    
    $scope.correctScore = function() {
    	var modalInstance = $modal.open({
    		templateUrl: '/static/battle/templates/scoreCorrection.html',
    	      controller: CorrectScoreCtrl,
    	      resolve: {
    	        score: function () {
    	          return $scope.currentFight.totalScore();
    	        }
    	      }
    	});
    	
    	modalInstance.result.then(function(correction) {
    		correction.time = $scope.timerValue();
    		$scope.pushCorrection(correction);
    	}, function() {
    		// modal cancelled, do nothing
    	});
    };
    }
    
    $scope.exchangeLimitReached = function() {
    	return $scope.currentFight.started && $scope.currentFight.exchangeLimit > 0 && $scope.currentFight.totalScore().x >= $scope.currentFight.exchangeLimit;
    };
    
    $scope.timeLimitReached = function() {
    	return $scope.currentFight.started && $scope.timerValue() >= $scope.currentFight.timeLimitOfFight;
    };
    
    $scope.doubleHitLimitReached = function() {
    	return $scope.currentFight.started && $scope.currentFight.totalScore().d >= 5;
    };
    
    $scope.sendUpdate = function() {
    	playRoutes.controllers.AdminInterface.fightUpdate().post($scope.currentFight);
    };
  
    $scope.startFight = function() {
		$scope.resetTimer();
    	
		if ($scope.currentFight.globalOrder > -1) {
	    	$scope.currentFight.started = true;
	    	$scope.currentFight.timeStart = Date.now();
	    	$scope.timer.currentTime = _.reduce($scope.currentFight.scores, function(memo, score) { return Math.max(score.timeInFight, memo); }, 0);
	    	$scope.timer.displayTime = $scope.timerValue();
	    	
	    	$scope.sendUpdate();
	    	
	        playRoutes.controllers.AdminInterface.timerUpdate($scope.currentFight.phaseType, $scope.currentFight.id).post({action: $scope.timer.running ? "start" : "stop", time: $scope.timer.currentTime});

	        var next = $scope.findNextFight();
	    	if (next) {
	    		$scope.defaultAnnouncements.nextup = "Next up: <span class=\"label label-default\">" + next.tournament.memo + "</span> <span class=\"badge red\">" + next.fighterA.participant.fighterNumber + "</span> <b>" + next.fighterA.label  + "</b> vs <span class=\"badge blue\">" + next.fighterB.participant.fighterNumber + "</span> <b>" + next.fighterB.label + "</b> at " + $filter('hours')(next.time);
	    	} else {
	    		$scope.defaultAnnouncements.nextup = "";
	    	}
	    	$scope.defaultAnnouncement('nextup');
    	}
    };
    
    $scope.findCurrentFight = function() {
    	return _.find($scope.fights, function(fight) { return fight.timeStop == 0 && !fight.postponed; });
    };

    $scope.findNextFight = function() {
    	return _.find($scope.fights, function(fight) { return fight.timeStop == 0 && !fight.postponed && fight.globalOrder > $scope.currentFight.globalOrder; });
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
    
    $scope.postponeFight = function() {
    	if ($scope.currentFight.globalOrder > -1) {
    		$scope.stopTimer();
    		
    		$scope.currentFight.postponed = true;
    		
    		playRoutes.controllers.AdminInterface.fightPostpone().post($scope.currentFight).success(function(data, status) {
    			$scope.currentFight = $scope.findCurrentFight();
    		});
    	}
    };
    
    $scope.announce = function() {
    	$scope.announcement = $scope.announcementBuffer;
    	$scope.announcementBuffer = '';
    };
    
    $scope.defaultAnnouncements = {
    	nextup: ""	
    };
    
    $scope.defaultAnnouncement = function(key) {
    	$scope.announcement = $scope.defaultAnnouncements[key];
    };
    
    $scope.$watch('currentFight', function(newValue, oldValue) {
    	if (newValue && newValue.tournament) {
    		$rootScope.title = newValue.tournament.name;
    	}
		$scope.fightsShowing[0] = Math.max($scope.currentFight.globalOrder - 2, 1);
    	$scope.fightsShowing[1] = Math.min($scope.fightsShowing[0] + 4, $scope.fights.length);
    	$scope.fightsShowing[0] = Math.max($scope.fightsShowing[1] - 4, 1);
    });
    
    $scope.$watch('announcement', function(newValue, oldValue) {
    	if ($scope.currentFight.id) {
    		playRoutes.controllers.AdminInterface.messageUpdate($scope.currentFight.phaseType, $scope.currentFight.id).post(JSON.stringify(newValue));
    	}
    });
    
    $(window).on("beforeunload", function() {
    	return "You might lose some information by doing this!";
    });
    
    $('#score-options').hide();
};

var ExchangeListCtrl = function($scope, $modalInstance, exchanges) {
	$scope.exchanges = exchanges;
	var exchangeCounter = 0;
	_.each($scope.exchanges, function(score) {
		if (score.exchanges != 0) {
			exchangeCounter += score.exchanges;
			score.exchangeId = exchangeCounter;
		} else {
			score.exchangeId = "-";
		}
	});
	
	$scope.close = function() {
		$modalInstance.dismiss('cancel');
	};
};

var CorrectScoreCtrl = function($scope, $modalInstance, score) {
	$scope.score = score;
	$scope.correction = {a: 0, b: 0, d: 0};
	
	$scope.inc = function(field) {
		$scope.correction[field] += 1;
	}
	
	$scope.dec = function(field) {
		$scope.correction[field] -= 1;
	}
	
	$scope.confirm = function() {
		$modalInstance.close($scope.correction);
	};

	$scope.cancel = function() {
		$modalInstance.dismiss('cancel');
	};
}

var PoolsCtrl = function($rootScope, $scope, $timeout, $location, playRoutes, appService) {
	$rootScope.title = "Loading...";
	$rootScope.subtitle = "";
	
	$scope.arenas = new Array();
	var _ = window._;

	playRoutes.controllers.AdminInterface.event().get().success(function(data) {
		$rootScope.title = JSON.parse(data);
	});
	
	playRoutes.controllers.AdminInterface.arenas().get().success(function(data, status) {
		$scope.arenas = _.map(data, function(arena) { arena.fetchedFights = new Array(); return arena; });
		_.each($scope.arenas, function(arena) {
			playRoutes.controllers.AdminInterface.arena(arena.id).get().success(function(data, status) {
				console.log(JSON.stringify(data));
				arena.fetchedFights = _.map(data, function(fight) {
					fight.fight.finished = fight.fight.timeStop > 0;
					return fight;
				});
			});
		});
	}).error(function(data, status, headers) {
		$scope.arenas = "Error " + status + ": " + headers();
	});
	
	$scope.subscribe = function(arena) {
		$rootScope.arena = _.omit(arena, "fetchedFights");
		$location.path("/fight");
	};
};
