angular.module('htm.tournament').controller('TournamentsListCtrl', function($scope, $meteor) {
  $scope.tournaments = $meteor.collection(Tournaments);
})