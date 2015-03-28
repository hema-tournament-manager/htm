'use strict';

/* App Module */
(function(){
	angular.module('htm.App', [
		'ngRoute','ngMockE2E',
		'ui.bootstrap',
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
				templateUrl: '/partials/participant.html',
			})
			.otherwise({
				redirectTo: '/',
				templateUrl: '/partials/welcome.html',
			});
	}])


	// if(document.URL.match(/\?nobackend$/)) {
		console.log('======== USING STUBBED BACKEND ========');

		 angular.module('htm.App')



			.run(function($httpBackend) {
				var tournaments = [{
					id: 1,
					name: "Longsword",
					memo: "LS", 
					participants: [1,2,3,4,5],
				},{
					id: 2,
					name: "Dagger",
					memo: "DG", 
					participants: [1,2,3],
				}, {
					id: 3,
					name: "Sword and shield",
					memo: "SAS", 
					participants: [],
				}];

				var participants = [	{  	id: 1,
		  				externalId: 'externalId',  				
		  				name: 'name',
		  				shortName: 'shortName',
		  				club: 'club',
		  				clubCode: 'String',
						country: 'NL',
						isPresent: true,
						tshirt: 'String',
						age: 'Int',
						height: 'Int',
						weight: 'Int',
						previousWins: ['previousWins','previousWins'],

						subscriptions: [{
							fighterNumber: 1,
							gearChecked: true,
							droppedOut: true,
							pool: 'Swimming',
							tournament: {
								id: 1, name: 'Dagger', memo: 'DG'
							}
						},{
							fighterNumber: 1,
							gearChecked: false,
							droppedOut: false,
							pool: 'Swimming',
							tournament: {
								id: 2, name: 'Longsword', memo: 'LS'
							}
						}],
						hasPicture: true
					},{  id:  2,
		  				externalId: 'externalId',  				
		  				name: 'name',
		  				shortName: 'shortName',
		  				club: 'club',
		  				clubCode: 'String',
						country: 'NL',
						isPresent: true,
						tshirt: 'String',
						age: 'Int',
						height: 'Int',
						weight: 'Int',
						previousWins: ['previousWins','previousWins'],

						subscriptions: [{
							fighterNumber: 2,
							gearChecked: true,
							droppedOut: false,
							pool: 'Swimming',
							tournament: {
								id: 1, name: 'Longsword', memo: 'LS'
							}
						},{
							fighterNumber: 2,
							gearChecked: false,
							droppedOut: false,
							pool: 'Swimming',
							tournament: {
								id: 2, name: 'Longsword', memo: 'LS'
							}
						}],
						hasPicture: false
					},{  id: 3,
		  				externalId: 'externalId',  				
		  				name: 'name',
		  				shortName: 'shortName',
		  				club: 'club',
		  				clubCode: 'String',
						country: 'NL',
						isPresent: true,
						tshirt: 'String',
						age: 'Int',
						height: 'Int',
						weight: 'Int',
						previousWins: ['previousWins','previousWins'],
						fighterNumber: 'fighterNumber',

						subscriptions: [{
							fighterNumber: 3,
							gearChecked: false,
							droppedOut: false,
							pool: 'Swimming',
							tournament: {
								id: 1, name: 'Longsword', memo: 'LS'
							}
						},{
							fighterNumber: 3,
							gearChecked: false,
							droppedOut: false,
							pool: 'Swimming',
							tournament: {
								id: 2, name: 'Longsword', memo: 'LS'
							}
						}],
						hasPicture: false
					},{  id: 4,
		  				externalId: 'externalId',  				
		  				name: 'name',
		  				shortName: 'shortName',
		  				club: 'club',
		  				clubCode: 'String',
						country: 'NL',
						isPresent: true,
						tshirt: 'String',
						age: 'Int',
						height: 'Int',
						weight: 'Int',
						previousWins: ['previousWins','previousWins'],
						fighterNumber: 'fighterNumber',

						subscriptions: [{
							fighterNumber: 4,
							gearChecked: true,
							droppedOut: true,
							pool: 'Swimming',
							tournament: {
								id: 1, name: 'Longsword', memo: 'LS'
							}
						}],
						hasPicture: false
					},{  id: 5,
		  				externalId: 'externalId',  				
		  				name: 'name',
		  				shortName: 'shortName',
		  				club: 'club',
		  				clubCode: 'String',
						country: 'NL',
						isPresent: false,
						tshirt: 'String',
						age: 'Int',
						height: 'Int',
						weight: 'Int',
						previousWins: ['previousWins','previousWins'],
						subscriptions: [{
							fighterNumber: 5,
							gearChecked: false,
							droppedOut: false,
							pool: 'Swimming',
							tournament: {
								id: 1, name: 'Longsword', memo: 'LS'
							}
						}],
						hasPicture: true
					}]

		  		$httpBackend.whenGET(/^\/partials\//).passThrough();

			 	$httpBackend.whenGET('/api/tournament').respond(tournaments);
			 	$httpBackend.whenPOST('/api/tournament').respond(function(method, url, data) {
   					var tournament = angular.fromJson(data);
    				tournaments.push(tournament);
    				return [200, tournament, {}];
  				});
			 	$httpBackend.whenGET('/api/participant').respond(participants);



			});

		


	// }


})();