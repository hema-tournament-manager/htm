'use strict';

angular.module('htm', ['common.playRoutes', 'common.filters', 'ui.bootstrap']).
controller('ControllerCtrl', ['$rootScope', '$scope', '$timeout', '$modal', '$location', 'playRoutes', ControllerCtrl]).
	config(
		[ '$routeProvider', function($routeProvider) {
			$routeProvider.when('/controller', {
				templateUrl : 'static/viewer/templates/controller.html',
				controller : 'ControllerCtrl'
			}).otherwise({
				redirectTo : '/controller'
			});
		} ]);