'use strict';

angular
		.module(
				'htm',
				[ 'common.playRoutes', 'common.filters', 'htm.services',
						'ui.bootstrap' ]).controller('EmptyCtrl',
				[ '$scope', '$location', 'playRoutes', EmptyCtrl ]).controller(
				'FightCtrl', [ '$scope', 'playRoutes', FightCtrl ]).config(
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
						function($rootScope, $location, $http) {
							$rootScope.switchView = function(viewMsg) {
								var view = viewMsg.data.replace(/'/g, "");
								$rootScope.$apply(function() { 
									$location.path("/" + view); 
								});
							};
							
							$rootScope.switchFeed = new EventSource("/switchFeed");
							$rootScope.switchFeed.addEventListener("message",
									$rootScope.switchView, false);
						} ]);
;