'use strict';

/* App Module */
(function(){
	angular.module('htm.App', ['ngRoute','ui.bootstrap','htm.ui','htm.api','htm.tournament','ngMockE2E'])

	.config(['$routeProvider', function($routeProvider) {
		$routeProvider.
			when('/tournament', {
				templateUrl: '/partials/tournament-list.html',
			}).
			when('/tournament/:tournamentId', {
				templateUrl: '/partials/tournament.html',
			}).
			otherwise({
				redirectTo: '/',
				templateUrl: '/partials/welcome.html',
			});
	}]);

	// if(document.URL.match(/\?nobackend$/)) {
		console.log('======== USING STUBBED BACKEND ========');

		 angular.module('htm.App')
			.run(function($httpBackend) {
				var tournaments = [];

		  		$httpBackend.whenGET(/^\/partials\//).passThrough();

			 	$httpBackend.whenGET('/api/tournament').respond(tournaments);
			 	$httpBackend.whenPOST('/api/tournament').respond(function(method, url, data) {
   					var tournament = angular.fromJson(data);
    				tournaments.push(tournament);
    				return [200, tournament, {}];
  				});


			});


	// }


})();