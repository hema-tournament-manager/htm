'use strict';
(function(){

/* Directives */
	angular.module('htm.ui', [])
		
		/**
		 *	Usage:  <htm-navigation></htm-navigation>
		 *
		 *	Is replaced with the HTM top navigation
		 *
		 */
		.directive('htmNavigation', function() {
			return {
				restrict: 'E',
				replace: true,
				templateUrl:'/partials/navigation.html'
			}
		})
		/**
		 *	Usage:  <htm-tournament-list-item tournament=[data]></htm-tournament-list-item>
		 *
		 *	Is replaced the tournment list item bound to data.
		 *
		 */
		.directive('htmTournamentListItem', function() {
			return {
				restrict: 'E',
				replace: true,
				scope: "=",
				templateUrl:'/partials/tournament-list-item.html',
				controller: function($scope){

					$scope.color = function(tournament) {
						if(angular.isDefined(tournament.color)){
							return tournament.color;
						}

						var str = tournament.name || ''; 	

						var hash = 0;
						for (var i = 0; i < str.length; i++) {
							hash = str.charCodeAt(i) * 71 +  ((hash << 5) - hash);
						}
						// Work around for javascripts wonky modulo
						var hue = ((hash % 240) + 240)% 240;
						
						tournament.color = tinycolor({h: hue,s: 100,v: 50}).toHexString();					
						
						return tournament.color;
					};

				}

			}
		})
		/**
		 *	Usage: <ANY htm-focus-on=[binding]></ANY>
		 *
		 *	Will call focus to the atribute when the binding becomes truthy.
		 *
		 */
		.directive("htmFocusOn", ['$timeout', function($timeout) {
		 	return {
				restrict : 'A',
				link : function($scope,element,attr) {
	 				
	 				$(element).focus(function(){this.select();});

					$scope.$watch(attr.htmFocusOn,function(_focusVal) {
						if(_focusVal){
						 	$timeout(function() {
								$(element).focus();
							});
						}
					 });
				}
		 	}
		}])
})();