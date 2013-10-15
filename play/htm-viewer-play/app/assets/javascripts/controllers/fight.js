"use strict";

var FightCtrl = function($scope, playRoutes) {
	$scope.fight = {
		fighterA: {name: "Fighter A"},
		fighterB: {name: "Fighter B"},
		totalScore: function() {
			return {a: 0, b: 0, d: 2};
		}
	};
	
	$scope.range = function(n) {
        return new Array(n);
    };
};