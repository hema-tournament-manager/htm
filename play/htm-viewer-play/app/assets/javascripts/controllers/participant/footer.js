"use strict";

var ParticipantFooterCtrl = function($scope, stateService) {
	_.extend($scope, stateService.get("participant/footer"));
	
	stateService.change(function(view, state, update) {
		if (view == "participant/footer") {
			_.extend($scope, state);
		}
	});
};