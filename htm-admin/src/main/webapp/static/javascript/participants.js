angular.module('htm', ['ngAnimate', 'ngResource'])
  .factory('Participant', ["$resource", function($resource){
    return $resource('/api/participants/:id', { "id" : "@id" }, { update: { method: 'PUT' }});
  }])
  .factory('Tournament', ['$resource', function($resource){
    return $resource('/api/tournaments/:id', { "id" : "@id" }, { update: { method: 'PUT' }});
  }])
  .directive('htmSubscriptionLabel', function() {
    return {
      restrict: 'E',
      scope: {
        person: '=',
        tournament: '='
      },
      controller: function($scope) {
        $scope.sub = _($scope.person.subscriptions).find(function(sub) { return sub.tournament.id === $scope.tournament.id; });
        
        console.log('subscriptionlabel', $scope.person);
      },
      template: '<a href="/tournaments/view/{{tournament.identifier}}#participant{{person.id}}" class="label" ng-show="sub" ng-class="sub.gearChecked ? \'label-success\' : \'label-danger\'" title="Fighter number {{sub.fighterNumber}} in {{tournament.name}}"><span class="glyphicon glyphicon-cog" ng-hide="sub.gearChecked"/> {{sub.fighterNumber}}</a>'
    };
  })
	.controller('ParticipantsCtrl', function($scope, $http, Participant, Tournament) {
    $scope.participants = Participant.query();
    $scope.tournaments = Tournament.query();
    $scope.tournaments.$promise.then(function() {
      console.log($scope.tournaments);
    });
	  
	  $scope.searchFilter = function(obj) { 
	    var re = new RegExp($scope.search, 'i');
	    return !$scope.search
	    	|| re.test(obj.name)
	    	|| re.test(obj.externalId)
	    	|| re.test(obj.club)
	    	|| re.test(obj.clubCode)
	    	|| re.test(obj.country)
	    	|| _(obj.subscriptions).some(function(subscription) {
	    		return re.test(subscription.tournament.name);
	    	});
	  };
    
    $scope.countClubs = function(participants) {
      return _(_(participants).groupBy(function (participant) { return participant.clubCode; })).size();
    };
    
    $scope.countCountries = function(participants) {
      return _(_(participants).groupBy(function (participant) { return participant.country; })).size();
    };
    
    $scope.hasDetails = function(participant) {
      return participant.age || participant.height || participant.weight;
    };
    
    $scope.participantPictures = {};
    $scope.hasPicture = function(participant) {
      if (!_($scope.participantPictures).has(participant.id)) {
        $scope.participantPictures[participant.id] = null;
        $http.get('/api/participants/' + participant.id + '/haspicture').success(function(data) {
          $scope.participantPictures[participant.id] = JSON.parse(data);
        });
      } 
      return $scope.participantPictures[participant.id];
    };
    
	});