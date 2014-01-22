"use strict";

var SelectedParticipantsCtrl = function($scope, $timeout, stateService) {
	var _ = window._;
	_.extend($scope, stateService.get("overview/selected_participants"));
	
	
	
	stateService.change(function(view, state) {

		if (view == "overview/selected_participants") {

		}
	});
};