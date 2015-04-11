/* App Module */
(function(){
'use strict';


	angular.module('htm.App',[
		'ngRoute','ngSanitize',
		'ui.bootstrap', 'ui.select',
		'htm.ui','htm.api','htm.tournament','htm.particpant'
		])

	.config(['$routeProvider', function($routeProvider) {
		$routeProvider.
			when('/tournament', {
				templateUrl: '/partials/tournament-list.html',
			})
			.when('/tournament/:tournamentId', {
				templateUrl: '/partials/tournament.html',
			})
			.when('/participant', {
				templateUrl: '/partials/participant-list.html',
			})
			.when('/participant/:participantId', {
				templateUrl: '/partials/participant-list.html',
			})
			.otherwise({
				redirectTo: '/',
				templateUrl: '/partials/welcome.html',
			});
	}]).config(function(uiSelectConfig) {
		uiSelectConfig.theme = 'bootstrap';
		uiSelectConfig.resetSearchInput = true;
	});

})();