'use strict';

angular
		.module(
				'htm',
				[ 'common.playRoutes', 'common.filters', 'htm.services',
						'ui.bootstrap' ]).controller('EmptyCtrl',
				[ '$scope', '$location', 'playRoutes', 'stateService', EmptyCtrl ]).controller(
				'FightCtrl', [ '$scope', 'playRoutes', 'stateService', FightCtrl ]).config(
				[ '$routeProvider', function($routeProvider) {
					$routeProvider.when('/empty', {
						templateUrl : 'assets/templates/empty.html',
						controller : 'EmptyCtrl'
					}).when('/fight', {
						templateUrl : 'assets/templates/fight.html',
						controller : 'FightCtrl'
					}).when('/overview', {
						templateUrl : 'partials/phone-detail.html',
						controller : 'PhoneDetailCtrl'
					}).otherwise({
						redirectTo : '/empty'
					});
				} ]).run(
				[
						"$rootScope",
						"$location",
						"$http",
						"stateService",
						function($rootScope, $location, $http, stateService) {
							$rootScope.updateView = function(updateMsg) {
								var update = JSON.parse(updateMsg.data);
								$rootScope.$apply(function() {
									console.log("received update for view " + update.view + ": " + JSON.stringify(update.payload));
									stateService.put(update.view, update.payload);
									$location.path("/" + update.view); 
								});
							};
							
							$rootScope.updateFeed = new EventSource("/updateFeed");
							$rootScope.updateFeed.addEventListener("message",
									$rootScope.updateView, false);
						} ]);
;