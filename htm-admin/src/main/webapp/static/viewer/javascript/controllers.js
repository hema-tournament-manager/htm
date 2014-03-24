"use strict";

var ControllerCtrl = function($rootScope, $scope, $timeout, $modal, $location, playRoutes) {
	$scope.viewers = new Array();
	$scope.arenas = false;
	$scope.images = false;
	$scope.announcement = "";
	$scope.announcementBuffer = "";
	$scope.participants = new Array();
	$scope.countries = new Array();
	$scope.tournaments = new Array();
	$scope.footer = {
		participant: {name: "", club: "", country: ""},
		participantBuffer: {name: "", club: "", country: ""}
	};
	
	$scope.hasViewers = function() {
		return _.findWhere($scope.viewers, {selected: true}) != undefined;
	};
	
	playRoutes.controllers.AdminInterface.viewers().get().success(function(data, status) {
		$scope.viewers = data;
		_.each($scope.viewers, function(viewer) { viewer.queue = new Array(); });
	});
	
	playRoutes.controllers.AdminInterface.arenas().get().success(function(data, status) {
		$scope.arenas = data;
	});
	
	playRoutes.controllers.AdminInterface.images().get().success(function(data, status) {
		$scope.images = data;
	});
	
	playRoutes.controllers.AdminInterface.participants().get().success(function(data, status) {
		$scope.participants = data;
	});
	
	playRoutes.controllers.AdminInterface.countries().get().success(function(data, status) {
		$scope.countries = data;
	});
	
	playRoutes.controllers.AdminInterface.tournaments().get().success(function(data, status) {
		$scope.tournaments = data;
		_.each($scope.tournaments, function(tournament) {
			tournament.unfinishedRounds = new Array();
			_.each(tournament.rounds, function(round) {
				if (!round.finished) {
					playRoutes.controllers.AdminInterface.round(round.id).get().success(function(data, status) {
						var newRound = {id: round.id, name: data.name};
						playRoutes.controllers.AdminInterface.roundFight(round.id).get().success(function(data, status) {
							newRound.fight = data;
							tournament.unfinishedRounds.push(newRound);
						});
					});
				}
			});
		});
		
	});
	
	$scope.announce = function() {
		$scope.announcement = $scope.announcementBuffer;
		$scope.announcementBuffer = '';
	};
	
	$scope.showFooter = function(participant) {
		$scope.footer.participant = _.pick(participant, "name", "club", "country");
		$scope.footer.participantBuffer = {name: "", club: "", country: ""};
	};
	
	$scope.update = function(summary, view, payload) {
	  var viewers = _.where($scope.viewers, {selected: true});
    _.each(viewers, function(viewer) {
      var data = {"view": view, "viewers": [viewer.id], "payload": payload};
      viewer.queue.push({"summary": summary, "data": data});
    });
//		console.log("Sending " + JSON.stringify(data));
//		playRoutes.controllers.AdminInterface.viewerUpdate().post(data);
	};
	
  $scope.showQueuedItem = function(viewer, index) {
    var queuedItem = viewer.queue[index];
    console.log("Sending " + JSON.stringify(queuedItem.data));
    playRoutes.controllers.AdminInterface.viewerUpdate().post(queuedItem.data);
    viewer.lastUpdate = queuedItem.summary;
    viewer.queue.splice(index, 1);
  }
  
  $scope.removeQueuedItem = function(viewer, index) {
    viewer.queue.splice(index, 1);
  }
  
	$scope.$watch('announcement', function(newValue, oldValue) {
		$scope.update("Message", "", {message: newValue});
    });
	
	$scope.$watch('footer.participant', function(newValue, oldValue) {
		$scope.update("Footer: " + newValue.name, "participant/footer", {participant: newValue});
    });
	
	$scope.onKeypress = function(event) {
	  if (event.keyCode >= 49 && event.keyCode <= 48 + $scope.viewers.length) {
  	  $scope.$apply(function() {
  	    var viewer = $scope.viewers[event.keyCode - 49];
  	    if (event.ctrlKey) {
  	      viewer.selected = !viewer.selected;
  	      event.preventDefault();
  	    } else {
    	    if (viewer.queue.length == 0) {
    	      viewer.queue.push({"summary": "Empty", "data": {"view": "empty", "viewers": [viewer.id], "payload": ""}});
    	    }
    	    $scope.showQueuedItem(viewer, 0);
  	    }
  	  });
	  } 
	};
	
	document.addEventListener('keydown', $scope.onKeypress, false);
};
