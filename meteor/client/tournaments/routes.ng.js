angular.module('htm.tournament').config(function($urlRouterProvider, $stateProvider, $locationProvider) {
  $locationProvider.html5Mode(true);

  $stateProvider
    .state('tournaments', {
      url: '/tournaments',
      templateUrl: 'client/tournaments/views/tournaments-list.ng.html',
      controller: 'TournamentsListCtrl'
    })
    .state('tournamentView', {
      url: '/tournaments/:tournamentId',
      templateUrl: 'client/tournaments/views/tournament-view.ng.html',
      controller: 'TournamentViewCtrl'
    });

  $urlRouterProvider.otherwise('/tournaments');
});