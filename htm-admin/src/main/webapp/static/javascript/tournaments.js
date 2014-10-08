var htmTournaments = angular.module('htm-tournaments', ['htm']);

htmTournaments.controller('TournamentsCtrl', ['$scope', 'Tournament', 'focus', function($scope, Tournament, focus) {
  $scope.tournaments = Tournament.query();
  console.log('tournaments', $scope.tournaments);
  
  $scope.newTournament = {
    name: '',
    customIdentifier: false,
    identifier: '',
    defaultIdentifier: function() {
      return this.name.toLowerCase().split(' ').join('_');
    },
    customMemo: false,
    memo: '',
    defaultMemo: function() {
      var memo = '';
      angular.forEach(this.name.toUpperCase().split(' '), function(s) {
        memo += s.charAt(0);
      });
      return memo;
    },
    reset: function() {
      this.name = 'Basic Longsword Tournament',
      this.customIdentifier = false;
      this.identifier = '';
      this.customMemo = false;
      this.memo = '';
    }
  };
  $scope.newTournament.reset();
  
  $scope.save = function() {
    var t = $scope.newTournament;
    var tournament = new Tournament({
      name: t.name,
      identifier: t.customIdentifier ? t.identifier : t.defaultIdentifier(),
      memo: t.customMemo ? t.memo : t.defaultMemo(),
      participants: []
    });
    $scope.newTournament.reset();
    $scope.tournaments.push(tournament);
    tournament.$save();
  };
  
  $scope.$watch('newTournament.customIdentifier', function(newValue) {
    if (newValue) {
      focus('identifier');
    }
  });
  $scope.$watch('newTournament.customMemo', function(newValue) {
    if (newValue) {
      focus('memo');
    }
  });
  
  $scope.openNewTournament = function() {
    focus('createNew');
  };
}]);

htmTournaments.directive('htmParticipantsBar', [function() {
  return {
    restrict: 'E',
    scope: {
      count: '@'
    },
    link: function(scope, element, attrs) {
      for (var i = 0; i < +scope.count; i++) {
        var img = Math.floor(Math.random() * 32) + 1;
        if (img < 10) { img = '0' + img; }
        element.append('<img src="/images/nethackers/' + img + '.png" style="vertical-align: bottom;' + (i > 0 ? ' margin-left: -8px;' : '') + '"/>');
      }
    }
  }
}]);