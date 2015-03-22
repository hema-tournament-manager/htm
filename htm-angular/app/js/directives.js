'use strict';
(function(){

/* Directives */
	angular.module('htm.ui', [])

		.directive('htmNavigation', function() {
			return {
				restrict: 'E',
				replace: true,
				templateUrl:'partials/navigation.html'
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