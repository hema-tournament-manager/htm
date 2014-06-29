angular.module('htm', ['ngAnimate', 'ngResource', 'ui.bootstrap', 'ui.select2'])
  .factory('Participant', ["$resource", function($resource){
    return $resource('/api/participants/:id', { "id" : "@id" }, { update: { method: 'PUT' }});
  }])
  .factory('Tournament', ['$resource', function($resource){
    return $resource('/api/tournaments/:id', { "id" : "@id" }, { update: { method: 'PUT' }});
  }])
  .factory('Country', ['$resource', function($resource){
    return $resource('/api/countries/:id', { "id" : "@id" }, { update: { method: 'PUT' }});
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
      },
      template: '<a href="/tournaments/view/{{tournament.identifier}}#participant{{person.id}}" class="label" ng-show="sub" ng-class="sub.gearChecked ? \'label-success\' : \'label-danger\'" title="Fighter number {{sub.fighterNumber}} in {{tournament.name}}"><span class="glyphicon glyphicon-cog" ng-hide="sub.gearChecked"/> {{sub.fighterNumber}}</a>'
    };
  })
	.controller('ParticipantsCtrl', function($scope, $http, $modal, Participant, Tournament) {
    $scope.participants = Participant.query();
    $scope.tournaments = Tournament.query();
	  
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
    
    $scope.register = function(participant) {
      var size = 'lg';
      var modalInstance = $modal.open({
        templateUrl: '/static/templates/participant-registration-modal.template',
        controller: 'ParticipantRegistrationModalCtrl',
        size: size,
        resolve: {
          participant: function () {
            return participant;
          },
          tournaments: function() {
            return $scope.tournaments;
          }
        }
      });
      
      modalInstance.result.then(function(updatedParticipant) {
        updatedParticipant.$save(function() {
          angular.copy(updatedParticipant, participant);
        });
      });
      
    };
    
    
	})
	  .controller('ParticipantRegistrationModalCtrl', function($scope, $modalInstance, participant, tournaments, Country) {
	    $scope.participant = angular.copy(participant);
	    $scope.participant.previousWins = _($scope.participant.previousWins).map(function(win) { return {text: win}; });
	    $scope.tournaments = tournaments;
	    $scope.countries = Country.query();
	    
	    $scope.subscribed = function(participant, tournament) {
	      return _(participant.subscriptions).some(function(sub) { return sub.tournament.id == tournament.id; });
	    };
	    
      $scope.addWin = function() {
        if ($scope.canAddWin()) {
          $scope.participant.previousWins.push({text: ''});
        }
      };
      
      $scope.canAddWin = function() {
        return $scope.participant.previousWins.length < 3 && _($scope.participant.previousWins).every(function(win) { return win.text && win.text.length > 0; });
      };
      
	    $scope.ok = function() {
	      $scope.participant.previousWins = _($scope.participant.previousWins).map(function(win) { return win.text; });
	      $modalInstance.close($scope.participant);
	    };
	    
	    $scope.cancel = function() {
	      $modalInstance.dismiss('cancel');
	    };
	  });