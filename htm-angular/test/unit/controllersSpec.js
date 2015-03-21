'use strict';

/* jasmine specs for controllers go here */

// testing controller
describe('TournamentListCtrl', function() {
	var $httpBackend, $rootScope, createController, authRequestHandler;

	// Set up the module
	beforeEach(module('htm.tournament'));

	beforeEach(inject(function($injector) {
	  // Set up the mock http service responses
	  $httpBackend = $injector.get('$httpBackend');
	  // backend definition common for all tests
	  authRequestHandler = $httpBackend.when('GET', '/api/tournaments')
									 .respond([{
												  name: 'Test Tournament 1',
												  identifier: 'test-tournament-1',
												  memo: 'TT1',
												  participants: []
												},{
												  name: 'Test Tournament 2',
												  identifier: 'test-tournament-2',
												  memo: 'TT2',
												  participants: []
												}]);

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

	  $httpBackend.expectGET('/api/tournaments');
	  var controller = createController();

	  $httpBackend.flush();

	});

	it('should create a new tournament', function() {
		$httpBackend.expectPOST('/api/tournaments').respond('201','');
		var controller = createController();
		$rootScope.newTournament.name = 'New Tournament'
		$rootScope.save();
 
		//test saving

		$httpBackend.flush();

		// test done saving
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