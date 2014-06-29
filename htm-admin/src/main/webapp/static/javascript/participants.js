angular.module('htm', ['ngAnimate', 'ngResource', 'ui.bootstrap', 'ui.select2', 'ui.keypress'])
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
  .directive('htmFile', ['$parse', '$timeout', function ($parse, $timeout) {
    return {
      restrict: 'A',
      link: function(scope, element, attrs) {
        var model = $parse(attrs.htmFile);
        var modelSetter = model.assign;
        
        element.bind('change', function(){
          scope.$apply(function() {
            modelSetter(scope, element[0].files[0]);
            // $watch-ing the model var did not work, so we're emitting a custom change event 
            scope.$emit('FileChanged', element[0].files[0]);
          });
        });
      }
    };
  }])
  .service('htmFileUpload', ['$http', function ($http) {
    this.uploadFileToUrl = function(file, uploadUrl){
      var fd = new FormData();
      fd.append('file', file);
      return $http.post(uploadUrl, fd, {
        transformRequest: angular.identity,
        headers: {'Content-Type': undefined}
      });
    }
  }])
	.controller('ParticipantsCtrl', function($scope, $http, $modal, $filter, Participant, Tournament) {
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
    
    $scope.registerSelected = function() {
      var filtered = $filter('filter')($scope.participants, $scope.searchFilter);
      if (filtered.length == 1) {
        $scope.register(filtered[0]);
      }
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
	  .controller('ParticipantRegistrationModalCtrl', function($scope, $modalInstance, $http, htmFileUpload, Country, participant, tournaments) {
	    $scope.participant = angular.copy(participant);
	    $scope.participant.previousWins = _($scope.participant.previousWins).map(function(win) { return {text: win}; });
	    $scope.tournaments = _(angular.copy(tournaments)).map(function(tournament) {
	      var sub = _(participant.subscriptions).find(function(sub) { return sub.tournament.id == tournament.id; });
	      if (sub) {
	        return _(tournament).extend({subscription: sub});
	      } else {
	        return tournament;
	      }
	    });
	    
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
      
      $scope.removeWin = function(index) {
        $scope.participant.previousWins.splice(index, 1);
      };
      
	    $scope.save = function() {
	      $scope.participant.previousWins = _($scope.participant.previousWins).map(function(win) { return win.text; });
	      $modalInstance.close($scope.participant);
	    };
	    
	    $scope.checkin = function() {
	      $scope.participant.isPresent = true;
	      $scope.save();
	    };
	    
	    $scope.cancel = function() {
	      $modalInstance.dismiss('cancel');
	    };
	    
	    $scope.pictureUrl = '/photo/' + participant.externalId + '/l';
	    $scope.$on('FileChanged', function(event, file) {
	      if (file) {
	        $scope.pictureUrl = '';
	        htmFileUpload.uploadFileToUrl(file, '/api/participants/' + $scope.participant.id + '/picture').success(function(data) {
	          $scope.pictureUrl = '/photo/' + participant.externalId + '/l';
	        });
	      }
	    });
	  });