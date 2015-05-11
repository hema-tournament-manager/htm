angular.module('htm.core').directive('htmFocusOn', function($timeout) {
  return {
    restrict : 'A',
    link : function($scope, element, attr) {
      $scope.$watch(attr.htmFocusOn, function(_focusVal) {
        if (_focusVal) {
          // Wrapped in timeout so event 
          // can trigger in new eval round 
          $timeout(function() {
            var selected = angular.element(element)[0];
            selected.focus();

            if(selected.select) {
              // select input text on focus
              selected.select();
            }
          });
        }
      });
    }
  };
});