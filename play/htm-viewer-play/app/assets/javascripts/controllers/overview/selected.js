"use strict";

var OverviewSelectedCtrl = function($scope, stateService) {
	var _ = window._;
	_.extend($scope, stateService.get("overview/selected"));
	
	stateService.change(function(view, state) {
		if (view == "overview/selected") {
		  _.extend($scope, state);
		}
	});
};