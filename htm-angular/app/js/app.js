'use strict';

/* App Module */
(function(){
	angular.module('htm.App', ['ngRoute','htm.tournament'])

	.config(['$routeProvider', function($routeProvider) {
		$routeProvider.
			when('/tournament', {
				templateUrl: 'partials/tournament-list.html',
				controller: 'TournamentListCtrl',
			}).
			when('/tournament/:tournamentId', {
				templateUrl: 'partials/tournament.html',
				controller: 'TournamentCtrl',
			}).
			otherwise({
				redirectTo: '/',
				templateUrl: 'partials/welcome.html',
				controller: 'WelcomeCtrl',
			});
	}]);
})();