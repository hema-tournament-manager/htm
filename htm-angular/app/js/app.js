'use strict';

/* App Module */
(function(){
	angular.module('htm.App',[
		'ngRoute','ngMockE2E','ngSanitize',
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
	})


	// if(document.URL.match(/\?nobackend$/)) {
		console.log('======== USING STUBBED BACKEND ========');

		 angular.module('htm.App')



			.run(function($httpBackend) {
				var countries = [
				{code2: "NL", name: "The Netherlands"},
				{code2: "DE", name: "Germany"}
				];

				var tournaments = [
				{
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
  				
		  				name: 'name',
		  				shortName: 'shortName',
		  				club: 'club',
		  				clubCode: 'String',
						country: {code2: "NL", name: "Netherlands"},
						isPresent: true,
						tshirt: 'String',
						age: 27,
						height: 188,
						weight: 82,
						previousWins: [ {text:'previousWins'},{text:'previousWins'}],

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
  				
		  				name: 'name',
		  				shortName: 'shortName',
		  				club: 'club',
		  				clubCode: 'String',
						country: {code2: "NL", name: "Netherlands"},
						isPresent: true,
						tshirt: 'String',
						age: 27,
						height: 188,
						weight: 82,
						previousWins: [ {text:'previousWins'},{text:'previousWins'}],

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
  				
		  				name: 'name',
		  				shortName: 'shortName',
		  				club: 'club',
		  				clubCode: 'String',
						country: {code2: "NL", name: "Netherlands"},
						isPresent: true,
						tshirt: 'String',
						age: 27,
						height: 188,
						weight: 82,
						previousWins: [ {text:'previousWins'},{text:'previousWins'}],
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
  				
		  				name: 'name',
		  				shortName: 'shortName',
		  				club: 'club',
		  				clubCode: 'String',
						country: {code2: "NL", name: "Netherlands"},
						isPresent: true,
						tshirt: 'String',
						age: 27,
						height: 188,
						weight: 82,
						previousWins: [ {text:'previousWins'},{text:'previousWins'}],
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
  				
		  				name: 'name',
		  				shortName: 'shortName',
		  				club: 'club',
		  				clubCode: 'String',
						country: {code2: "NL", name: "Netherlands"},
						isPresent: false,
						tshirt: 'String',
						age: 27,
						height: 188,
						weight: 82,
						previousWins: [ {text:'previousWins'},{text:'previousWins'}],
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
   					tournament.id = tournaments.length;
    				tournaments.push(tournament);
    				return [200, tournament, {}];
  				});

			 	$httpBackend.whenGET(/\/api\/participant\/[0-9]+/).respond(participants[0]);
			 	
			 	$httpBackend.whenPOST(/\/api\/participant\/[0-9]+/).respond(function(method, url, data) {
    				return [200];
  				});
			 	$httpBackend.whenPOST('/api/participant').respond(function(method, url, data) {
   					var participant = angular.fromJson(data);
   					participant.id = tournaments.length;
    				participants.push(participant);
    				return [200, participant, {}];
  				});
			 	$httpBackend.whenGET('/api/participant').respond(participants);

			 	$httpBackend.whenGET('/api/country').respond(countries);


			});

		


	// }


})();