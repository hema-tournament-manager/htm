angular.module('htm', ['ngAnimate', 'ngResource'])
	.factory('Participant', ["$resource", function($resource){
		return $resource('/api/participants/:id', { "id" : "@id" }, { update: { method: 'PUT' }});
	}])
	.controller('ParticipantsCtrl', function($scope, Participant) {
	  $scope.participants = Participant.query();
	  
	  $scope.searchFilter = function(obj) { 
	    var re = new RegExp($scope.search, 'i');
	    return !$scope.search
	    	|| re.test(obj.name)
	    	|| re.test(obj.externalId)
	    	|| re.test(obj.club)
	    	|| re.test(obj.clubCode)
	    	|| re.test(obj.country)
	    	|| _(obj.tournaments).some(function(tournament) {
	    		return re.test(tournament.name);
	    	});
	  };
	});