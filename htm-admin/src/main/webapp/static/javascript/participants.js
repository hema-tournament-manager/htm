angular.module('htm', ['ngAnimate', 'ngResource'])
	.factory('Participant', ["$resource", function($resource){
		return $resource('/api/participants/:id', { "id" : "@id" }, { update: { method: 'PUT' }});
	}])
	.controller('ParticipantsCtrl', function($scope, Participant) {
	  $scope.participants = Participant.query();
	});