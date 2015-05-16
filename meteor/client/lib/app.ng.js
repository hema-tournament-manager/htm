angular.module('htm.app', [
  'ui.router',
  'htm.core',
  'htm.event',
  'htm.tournament'
]);

angular.module('htm.app').config(function($urlRouterProvider, $stateProvider, $locationProvider) {
  $locationProvider.html5Mode(true);

  $stateProvider
    .state('htm', {
      abstract: true,
      views: {
        'eventpicker': {
          templateUrl: 'client/events/views/event-picker.ng.html',
          controller: 'EventPickerCtrl',
          controllerAs: 'picker'
        },
        '': {
          templateUrl: 'client/main.ng.html'
        }
      }
    });

  $urlRouterProvider.otherwise('/events');
});