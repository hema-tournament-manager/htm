angular.module('htm', ['ngResource', 'ui.bootstrap', 'ui.select2', 'ui.keypress', 'angular-loading-bar'])
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
  .directive('htmSortContainer', [function() {
    return {
      restrict: 'A',
      scope: {
        field: '=',
        descending: '='
      },
      controller: function($scope) {
        this.setField = function(newField) {
          $scope.$apply(function() {
            if (newField == $scope.field) {
              $scope.descending = !$scope.descending;
            } else {
              $scope.field = newField;
              $scope.descending = false;
            }
          });
        };
        this.scope = $scope;
      }
    }
  }])
  .directive('htmSort', [function() {
    return {
      require: '^htmSortContainer',
      restrict: 'A',
      scope: {},
      link: function(scope, element, attrs, containerCtrl) {
        var field = attrs.htmSort;
        scope.myField = field;
        scope.container = containerCtrl.scope;
        
        element.css({cursor: 'pointer'});
        
        element.bind('click', function() {
          containerCtrl.setField(field);
        });
      },
      transclude: true,
      template: '<small ng-if="myField == container.field" class="glyphicon" ng-class="container.descending ? \'glyphicon-chevron-up\' : \'glyphicon-chevron-down\'"></small> <span ng-transclude></span>'
    }
  }])
  .directive('htmFile', ['$parse', function ($parse) {
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
    $scope.participants = [];
    $scope.nextPage = false;
    $scope.search = '';
    $scope.totals = {
        participants: '?',
        clubs: '?',
        countries: '?'
    };
    
    $scope.reload = function() {
      $scope.participants = [];
      var first = '/api/v2/participants?itemsPerPage=15&q=' + $scope.search + '&orderBy=' + $scope.sort.field + '&order=' + ($scope.sort.descending ? 'DESC' : 'ASC');
      console.log('first', first);
      $scope.loadMore(first);
    };
    
    $scope.loadMore = function(page) {
      $http.get(page).success(function(data) {
        $scope.participants = $scope.participants.concat(data.participants);
        $scope.nextPage = data.next;
      });
    };
    
    $scope.reloadTotals = function() {
      $http.get('/api/v2/participants/totals').success(function (data) {
        $scope.totals.participants = data.participants;
        $scope.totals.clubs = data.clubs.length;
        $scope.totals.countries = data.countries.length;
      });
    };
    $scope.reloadTotals();
    
    $scope.tournaments = Tournament.query();
	  
    $scope.sort = {
      field: 'name',
      descending: false
    };
    
    $scope.$watch(function() { return $scope.sort.field + $scope.sort.descending + $scope.search; }, function() {
      $scope.reload();
    });
    
    $scope.hasDetails = function(participant) {
      return participant.age || participant.height || participant.weight;
    };
    
    $scope.participantPictures = {};

    $scope.registerSelected = function() {
      var filtered = $filter('filter')($scope.participants, $scope.searchFilter);
      if (filtered.length == 1) {
        $scope.register(filtered[0]);
      }
    };
    
    var showParticipant = function(participant, onSuccess) {
      var modalInstance = $modal.open({
        templateUrl: '/static/templates/participant-registration-modal.template',
        controller: 'ParticipantRegistrationModalCtrl',
        size: 'lg',
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
        $http.post('/api/participants/' + updatedParticipant.externalId, updatedParticipant).success(function(data) {
          console.log('save returned ', data);
          if (!updatedParticipant.id) {
            updatedParticipant.id = +data.id;
          }
          onSuccess(updatedParticipant);
        });
      });
    };
    
    $scope.addParticipant = function() {
      var p = new Participant({
        externalId: $scope.totals.participants + 1,
        name: '',
        shortName: '',
        club: '',
        clubCode: '',
        country: 'NL',
        isPresent: false,
        tshirt: '',
        age: 0,
        height: 0,
        weight: 0
      });
      
      showParticipant(p, function(updatedParticipant) {
        $scope.participants.push(updatedParticipant);
        $scope.reloadTotals();
      });
    };
    
    $scope.register = function(participant) {
      showParticipant(participant, function(updatedParticipant) {
        angular.copy(updatedParticipant, participant);
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
	          $scope.participant.hasPicture = true;
	        });
	      }
	    });
	  });