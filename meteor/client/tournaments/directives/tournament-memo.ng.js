angular.module('htm.tournament').directive('htmTournamentMemo', function() {
  return {
    restrict: 'E',
    replace: true,
    templateUrl: 'client/tournaments/directives/tournament-memo.ng.html',
    scope: { tournament: "="} ,
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
  };
});