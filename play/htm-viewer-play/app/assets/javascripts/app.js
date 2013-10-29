'use strict';

angular
		.module(
				'htm',
				[ 'common.playRoutes', 'common.filters', 'htm.services',
						'ui.bootstrap' ]).controller('EmptyCtrl',
				[ '$scope', '$location', 'playRoutes', 'stateService', EmptyCtrl ]).controller(
						'FightCtrl', [ '$scope', '$timeout', 'playRoutes', 'stateService', FightCtrl ]).controller(
								'OverviewArenaCtrl', [ '$scope', '$timeout', 'stateService', OverviewArenaCtrl ]).controller(
										'ImageCtrl', [ '$scope', 'stateService', ImageCtrl ]).controller(
												'ParticipantFooterCtrl', [ '$scope', 'stateService', ParticipantFooterCtrl ]).config(
				[ '$routeProvider', function($routeProvider) {
					$routeProvider.when('/empty', {
						templateUrl : 'assets/templates/empty.html',
						controller : 'EmptyCtrl'
					}).when('/image', {
						templateUrl : 'assets/templates/image.html',
						controller : 'ImageCtrl'
					}).when('/fight', {
						templateUrl : 'assets/templates/fight.html',
						controller : 'FightCtrl'
					}).when('/overview/arena', {
						templateUrl : 'assets/templates/overview/arena.html',
						controller : 'OverviewArenaCtrl'
					}).when('/participant/footer', {
						templateUrl : 'assets/templates/participant/footer.html',
						controller : 'ParticipantFooterCtrl'
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
									if (update.view) {
										console.log("received update for view " + update.view + ": " + JSON.stringify(update.payload));
										stateService.put(update.view, update.payload);
										$location.path("/" + update.view);
									} else {
										console.log("received update for current view: " + JSON.stringify(update.payload));
										stateService.broadcast(update.payload);
									}
								});
							};
							
							$rootScope.updateFeed = new EventSource("/updateFeed");
							$rootScope.updateFeed.addEventListener("message",
									$rootScope.updateView, false);
						} ]);
;