'use strict';

angular.module('htm', ['common.playRoutes', 'common.filters', 'htm.services', 'ui.bootstrap']).
controller('BattleCtrl', ['$scope', '$timeout', '$modal', 'playRoutes', 'appService', BattleCtrl]).
controller('PoolsCtrl', ['$scope', '$timeout', 'playRoutes', 'appService', PoolsCtrl]);