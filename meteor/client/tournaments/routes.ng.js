angular.module('htm.tournament').config(function($urlRouterProvider, $stateProvider, $locationProvider) {
  $stateProvider
    .state('htm.tournaments', {
      url: '/tournaments',
      templateUrl: 'client/tournaments/views/tournaments-list.ng.html',
      controller: 'TournamentsListCtrl'
    })
    .state('htm.tournamentView', {
      url: '/tournaments/:tournamentId',
      templateUrl: 'client/tournaments/views/tournament-view.ng.html',
      controller: 'TournamentViewCtrl'
    });
});