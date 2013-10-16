'use strict';

angular.module('htm', ['common.playRoutes', 'common.filters', 'htm.services', 'ui.bootstrap']).
controller('BattleCtrl', ['$rootScope', '$scope', '$timeout', '$modal', '$location', 'playRoutes', 'appService', BattleCtrl]).
controller('PoolsCtrl', ['$rootScope', '$scope', '$timeout', '$location', 'playRoutes', 'appService', PoolsCtrl]).config(
		[ '$routeProvider', function($routeProvider) {
			$routeProvider.when('/arenas', {
				templateUrl : 'assets/templates/arenas.html',
				controller : 'PoolsCtrl'
			}).when('/fight', {
				templateUrl : 'assets/templates/fight.html',
				controller : 'BattleCtrl'
			}).otherwise({
				redirectTo : '/arenas'
			});
		} ]);