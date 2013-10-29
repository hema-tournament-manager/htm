"use strict";

var ParticipantBioCtrl = function($scope, stateService) {
	_.extend($scope, stateService.get("participant/bio"));
	
	stateService.change(function(view, state, update) {
		if (view == "participant/bio") {
			_.extend($scope, state);
		}
	});
};