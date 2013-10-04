'use strict';

angular.module('htm', ['common.playRoutes', 'common.filters', 'htm.services']).
controller('BattleCtrl', ['$scope', '$timeout', 'appService', BattleCtrl]);