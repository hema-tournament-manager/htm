'use strict';

angular.module('htm', ['common.playRoutes', 'common.filters', 'htm.services', 'ui.bootstrap']).
controller('BattleCtrl', ['$scope', '$timeout', 'playRoutes', 'appService', BattleCtrl]).
controller('PoolsCtrl', ['$scope', '$timeout', 'playRoutes', 'appService', PoolsCtrl]);