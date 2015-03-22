'use strict';

/* jasmine specs for controllers go here */

// testing controller
describe('TournamentListCtrl', function() {
	var $httpBackend, $rootScope, createController, authRequestHandler;

	var tournaments = [{
						  name: 'Test Tournament 1',
						  identifier: 'test-tournament-1',
						  memo: 'TT1',
						  participants: []
						},{
						  name: 'Test Tournament 2',
						  identifier: 'test-tournament-2',
						  memo: 'TT2',
						  participants: []
						}];

	// Set up the module
	beforeEach(module('htm.tournament'));

	beforeEach(inject(function($injector) {
	  // Set up the mock http service responses
	  $httpBackend = $injector.get('$httpBackend');
	  // backend definition common for all tests
	  authRequestHandler = $httpBackend.when('GET', '/api/tournament')
									 .respond([]);

	  // Get hold of a scope (i.e. the root scope)
	  $rootScope = $injector.get('$rootScope');
	  // The $controller service is used to create instances of controllers
	  var $controller = $injector.get('$controller');

	  createController = function() {
		 return $controller('TournamentListCtrl', {'$scope' : $rootScope });
	  };
	}));

	afterEach(function() {
	  $httpBackend.verifyNoOutstandingExpectation();
	  $httpBackend.verifyNoOutstandingRequest();
	});


	it('should query for tournaments', function() {		
	  $httpBackend.expectGET('/api/tournament');
	  var controller = createController();
	  $httpBackend.flush();
	});

	it('should update tournaments with query results', function() {		
	  $httpBackend.expectGET('/api/tournament').respond(tournaments);
	  var controller = createController();
	  expect($rootScope.tournaments.length).toBe(0);
	  $httpBackend.flush();
	  expect($rootScope.tournaments.length).toBe(tournaments.length);
	});

	it('should be loading while quering for results', function() {		
	  var controller = createController();
	  expect($rootScope.isLoading()).toBe(true);
	  $httpBackend.flush();
	});

	it('should be loaded after loading results', function() {		
	  var controller = createController();
	  $httpBackend.flush();
	  expect($rootScope.isLoaded()).toBe(true);

  	});

	it('should be error when unable to load', function() {		
	  $httpBackend.expectGET('/api/tournament').respond(404,'Not found');

	  var controller = createController();
	  $httpBackend.flush();
	  expect($rootScope.isError()).toBe(true);

	});

	it('should create a new tournament', function() {
		var controller = createController();

		$rootScope.newTournament.name = 'New Tournament'
 		$httpBackend.expectPOST('/api/tournament').respond(201,'Created');

		$rootScope.save();

		expect($rootScope.tournaments.length).toBe(0);

		$httpBackend.flush();

		expect($rootScope.tournaments.length).toBe(1);
	});


	it('should have a new tournament with error', function() {
		var controller = createController();

		$rootScope.newTournament.name = 'New Tournament'
 		$httpBackend.expectPOST('/api/tournament').respond(404,'Not found');

		$rootScope.save();
  	  	expect($rootScope.newTournament.error).toBe(undefined);

		$httpBackend.flush();

  	 	expect($rootScope.newTournament.error).not.toBe(undefined);
	});

	it('should have a new tournament without error on second try', function() {
		var controller = createController();

		$rootScope.newTournament.name = 'New Tournament'
 		$httpBackend.expectPOST('/api/tournament').respond(404,'Not found');

		$rootScope.save();
		$httpBackend.flush();
  	 	expect($rootScope.newTournament.error).not.toBe(undefined);
 		
 		$httpBackend.expectPOST('/api/tournament').respond(201,'Created');
		$rootScope.save();
		$httpBackend.flush();

  	  	expect($rootScope.newTournament.error).toBe(undefined);

	});

	// it('should fail authentication', function() {

	//   // Notice how you can change the response even after it was set
	//   authRequestHandler.respond(401, '');

	//   $httpBackend.expectGET('/auth.py');
	//   var controller = createController();
	//   $httpBackend.flush();
	//   expect($rootScope.status).toBe('Failed...');
	// });


	// it('should send msg to server', function() {
	//   var controller = createController();
	//   $httpBackend.flush();

	//   // now you don’t care about the authentication, but
	//   // the controller will still send the request and
	//   // $httpBackend will respond without you having to
	//   // specify the expectation and response for this request

	//   $httpBackend.expectPOST('/add-msg.py', 'message content').respond(201, '');
	//   $rootScope.saveMessage('message content');
	//   expect($rootScope.status).toBe('Saving...');
	//   $httpBackend.flush();
	//   expect($rootScope.status).toBe('');
	// });


	// it('should send auth header', function() {
	//   var controller = createController();
	//   $httpBackend.flush();

	//   $httpBackend.expectPOST('/add-msg.py', undefined, function(headers) {
	//     // check if the header was send, if it wasn't the expectation won't
	//     // match the request and the test will fail
	//     return headers['Authorization'] == 'xxx';
	//   }).respond(201, '');

	//   $rootScope.saveMessage('whatever');
	//   $httpBackend.flush();
	// });
});