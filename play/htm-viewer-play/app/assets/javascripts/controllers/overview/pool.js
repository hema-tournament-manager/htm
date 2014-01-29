"use strict";

// inject the $scope, $timeout (for timing stuff) and stateService
var OverviewPoolCtrl = function($scope, $timeout, stateService) {
	// import underscorejs (http://underscorejs.org/) to get the extend() function
	var _ = window._;
	
	// merge the initial state into the $scope
	_.extend($scope, stateService.get("overview/pool"));

  var nextPoolTimeout = false;
  $scope.nextPool = function() {
    $scope.pools.push($scope.pools.shift());
    $scope.currentPool = $scope.pools[0];
    nextPoolTimeout = $timeout($scope.nextPool, 10000);
  }

  $scope.onChange = function() {
	  if (nextPoolTimeout) {
	    $timeout.cancel(nextPoolTimeout);
	  }
	  
	  $scope.currentPool = $scope.pools[0];
	  nextPoolTimeout = $timeout($scope.nextPool, 10000);
	};
	
	// get notified on state changes for this view
	stateService.change(function(view, state) {
		// merge the new state into the $scope
		if (view == "overview/pool") {
			_.extend($scope, state);
			$scope.onChange();
		}
	});
	
	$scope.onChange();
};