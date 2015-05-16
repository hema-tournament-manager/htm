angular.module('htm.event').config(function($urlRouterProvider, $stateProvider, $locationProvider) {
  $stateProvider
    .state('events', {
      parent: 'htm',
      url: '/events',
      templateUrl: 'client/events/views/events-list.ng.html',
      controller: 'EventsListCtrl'
    });
});