"use strict";

// inject the $scope, $timeout (for timing stuff) and stateService
var OverviewPoolCtrl = function($scope, $timeout, stateService) {
	// import underscorejs (http://underscorejs.org/) to get the extend() function
	var _ = window._;
	
	// merge the initial state into the $scope
	_.extend($scope, stateService.get("overview/pool"));

	// TODO: initialization and functions
	
	// get notified on state changes for this view
	stateService.change(function(view, state) {
		// merge the new state into the $scope
		if (view == "overview/pool") {
			_.extend($scope, state);
			// TODO: handle state change
		}
	});
};