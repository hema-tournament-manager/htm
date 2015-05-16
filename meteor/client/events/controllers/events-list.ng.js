angular.module('htm.event').controller('EventsListCtrl', function($scope, $meteor) {
  $scope.events = $meteor.collection(Events);

  $meteor.autorun($scope, function() {
    $scope.activeEvent = Session.get('event');
  });

});