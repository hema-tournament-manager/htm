angular.module('htm.tournament').controller('TournamentsListCtrl', function($scope, $meteor) {
  $scope.tournaments = $meteor.collection(Tournaments);

  $scope.addNewTournamentVisible = false;
  $scope.focusOnAddTournament = false;

  $scope.showAddNewTournament = function(){
    $scope.addNewTournamentVisible = true;
    $scope.focusOnAddTournament = false;
  };

  $scope.hideAddNewTournament = function(){
    $scope.addNewTournamentVisible = false;
    $scope.focusOnAddTournament = true;
  };

  $scope._findTournamentWithSameName = function(newTournament){
    return _.find($scope.tournaments, function(existingTournament){ 
      return existingTournament.name === newTournament.name; 
    });
  };

  $scope.newTournament = {
    _defaultName : 'Basic Longsword Tournament',
    name: '',
    memo: undefined,

    customMemo: false,
    error: undefined,

    updateMemo: function(memo){
      if(angular.isDefined(memo)){
        this.memo = memo;
        this.customMemo = true;
      }

      // Reset memo when not custom or undefined
      if(!this.customMemo && angular.isUndefined(memo)){
        this.memo = this._defaultMemo();
      }
      return this.memo;
    },

    _defaultMemo: function() {
      var memo = '';
      var name = this.name || '';

      angular.forEach(name.toUpperCase().split(' '), function(s) {
        if(isNaN(s)){
          memo += s.charAt(0);
        } else {
          memo += s;
        }
      });
      return memo;
    },

    _generateUniqueName: function(){
      var i = 2;
      while($scope._findTournamentWithSameName(this)){
        this.name =  this._defaultName + " " + i++;
      }
    },

    reset: function() {
      this.name = this._defaultName;
      this.customIdentifier = false;
      this._identifier = undefined;
      this.customMemo = false;
      this.memo = undefined;
      this.error = undefined;
      this._generateUniqueName();
      this.updateMemo();
    }
  };
  $scope.newTournament.reset();

  $scope.save = function() {
    var t = $scope.newTournament;
    var tournament = {
      name: t.name,
      memo: t.updateMemo(),
      participants: []
    };

    $scope.tournaments.save(tournament);
    $scope.newTournament.reset();
    $scope.hideAddNewTournament();
  };
});