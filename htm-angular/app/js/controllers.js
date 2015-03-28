'use strict';
(function(){

	angular.module('htm.welcome', [])

		.controller('WelcomeCtrl', ['$scope', function($scope) {
		}])

	angular.module('htm.tournament', ['htm.api'])

		.controller('TournamentListCtrl', ['$scope', 'Tournament', function($scope, Tournament) {
			$scope.tournaments = Tournament.query();

			$scope.tournaments.$promise.then(function(tournaments){
				$scope.newTournament.reset();
			},function(error){
				$scope.tournaments.error = error;
			});

			/* New tournament form is initially hidden */
			$scope.addNewTournamentVisible = false;
			$scope.focusOnAddTournament = false;

			$scope.isLoading = function(){
				return !$scope.tournaments.$resolved;
			}
			
			$scope.isError = function(){
				return $scope.tournaments.$resolved && angular.isDefined($scope.tournaments.error);
			}

			$scope.isLoaded = function(){
				return $scope.tournaments.$resolved && angular.isUndefined($scope.tournaments.error);
			}
			
			$scope.showAddNewTournament = function(){
				$scope.addNewTournamentVisible = true;
				$scope.focusOnAddTournament = false;
			}

			$scope.hideAddNewTournament = function(){
				$scope.addNewTournamentVisible = false;
				$scope.focusOnAddTournament = true;
			}

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
					};

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
				var tournament = new Tournament({
					name: t.name,
					memo: t.updateMemo(),
					participants: []
				});

				$scope.newTournament.state = 'saving';
				tournament.$save().then(function(savedTournament){
					$scope.tournaments.push(savedTournament);
					$scope.newTournament.reset();
					$scope.hideAddNewTournament();
				},function(error){
					$scope.newTournament.error = error;
				});					
			};
		}])

		.controller('TournamentCtrl', ['$scope', '$routeParams', 'Tournament', function($scope, $routeParams, Tournament) {

		}])

	angular.module('htm.particpant', [])

		.controller('ParticipantListCtrl', ['$scope','Tournament','Participant', function($scope,Tournament, Participant) {
			  
		  $scope.totals = {
		  	participants: 100,
		  	clubs: 1,
		  	countries: 10

		  }


		  $scope.participants = Participant.query();
		  $scope.tournaments = Tournament.query();
		
		}])

		.controller('ParticipantCtrl', ['$scope', function($scope) {

		}])
	;

})();