angular.module('htm.tournament').controller('TournamentViewCtrl', function($scope, $stateParams, $meteor) {
  $scope.tournament = $meteor.object(Tournaments, {$or: [{_id: $stateParams.tournamentId}, {identifier: $stateParams.tournamentId}]});
});