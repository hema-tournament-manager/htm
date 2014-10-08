var htmModel = angular.module('htm.model', ['ngResource']);
var htmFocus = angular.module('htm.focus', []);
var htmDirectives = angular.module('htm.directives', []);
var htmSort = angular.module('htm.sort', []);
var htmFileUpload = angular.module('htm.fileupload', []);
var htm = angular.module('htm', [
   'htm.model',
   'htm.focus',
   'htm.directives',
   'htm.sort',
   'htm.fileupload',
   'ui.bootstrap',
   'ui.select2',
   'ui.keypress',
   'angular-loading-bar'
]);

htmModel.factory('Participant', ["$resource", function($resource) {
  return $resource('/api/participants/:id', { "id" : "@id" }, { update: { method: 'PUT' }});
}]);
htmModel.factory('Tournament', ['$resource', function($resource) {
  return $resource('/api/tournaments/:id', { "id" : "@id" }, { update: { method: 'PUT' }});
}])
htmModel.factory('Country', ['$resource', function($resource) {
  return $resource('/api/countries/:id', { "id" : "@id" }, { update: { method: 'PUT' }});
}])

htmFocus.directive('focusOn', function() {
  return function(scope, elem, attr) {
    scope.$on('focusOn', function(e, name) {
      if (name === attr.focusOn) {
        elem[0].focus();
        elem[0].select();
      }
    });
  };
});
htmFocus.factory('focus', [ '$rootScope', '$timeout',
  function($rootScope, $timeout) {
    return function(name) {
      $timeout(function() {
        $rootScope.$broadcast('focusOn', name);
      });
    };
  }
]);

htmDirectives.directive('htmTournamentLabel', function() {
  return {
    restrict: 'E',
    scope: {
      tournament: '='
    },
    controller: function($scope) {
      var stringToColour = function(str) {
        var hash = 0;
        for (var i = 0; i < str.length; i++) {
          hash = str.charCodeAt(i) + ((hash << 5) - hash);
        }
        return tinycolor({
          h: hash % 240,
          s: 100,
          v: 50
        }).toHexString();
      };
      $scope.tournamentColour = stringToColour($scope.tournament.name);
    },
    template: '<a href="/tournaments/view/{{tournament.identifier}}" class="label" title="{{tournament.name}}" style="background-color: {{tournamentColour}};">{{tournament.memo}}</a>'
  }
});
htmDirectives.directive('htmSubscriptionLabel', function() {
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
});
htmSort.directive('htmSortContainer', [function() {
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
}]);
htmSort.directive('htmSort', [function() {
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
}]);
htmFileUpload.directive('htmFile', ['$parse', function ($parse) {
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
}]);
htmFileUpload.service('htmFileUpload', ['$http', function ($http) {
  this.uploadFileToUrl = function(file, uploadUrl){
    var fd = new FormData();
    fd.append('file', file);
    return $http.post(uploadUrl, fd, {
      transformRequest: angular.identity,
      headers: {'Content-Type': undefined}
    });
  }
}]);