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
	});
	
	$scope.announce = function() {
		$scope.announcement = $scope.announcementBuffer;
		$scope.announcementBuffer = '';
	};
	
	$scope.showFooter = function(participant) {
		$scope.footer.participant = _.pick(participant, "name", "club", "country");
		$scope.footer.participantBuffer = {name: "", club: "", country: ""};
	};
	
	$scope.update = function(friendlyLabel, view, payload) {
		var viewers = _.pluck(_.where($scope.viewers, {selected: true}), 'id');
		_.each($scope.viewers, function(viewer) { if (viewer.selected) viewer.lastUpdate = friendlyLabel; });
		var data = {"view": view, "viewers": viewers, "payload": payload};
		console.log("Sending " + JSON.stringify(data));
		playRoutes.controllers.AdminInterface.viewerUpdate().post(data);
	};
	
	$scope.$watch('announcement', function(newValue, oldValue) {
		$scope.update("Message", "", {message: newValue});
    });
	
	$scope.$watch('footer.participant', function(newValue, oldValue) {
		$scope.update("Footer: " + newValue.name, "participant/footer", {participant: newValue});
    });
};
