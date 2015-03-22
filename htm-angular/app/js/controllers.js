'use strict';
(function(){

	angular.module('htm.welcome', [])

		.controller('WelcomeCtrl', ['$scope', function($scope) {
		}])

	angular.module('htm.tournament', ['htm.api'])

		.controller('TournamentListCtrl', ['$scope', 'Tournament', function($scope, Tournament) {
			$scope.tournaments = Tournament.query();

			/* New tournament form is initially hidden */
			$scope.addNewTournamentVisible = false;

			$scope.showAddNewTournament = function(){
				$scope.addNewTournamentVisible = true;
			}

			$scope.hideAddNewTournament = function(){
				$scope.addNewTournamentVisible = false;
			}

			$scope._findTournamentWithSameId = function(newTournament){
				return _.find($scope.tournaments, function(existingTournament){ 
					return existingTournament.identifier === newTournament.identifier(); 
				});
			};

			$scope.color = function(tournament) {
				if(!angular.isDefined(tournament.color)){
					var str = tournament.name || ''; 	

					var hash = 0;
					for (var i = 0; i < str.length; i++) {
						hash = str.charCodeAt(i) * 71 +  ((hash << 5) - hash);
					}
					// Work around for javascripts wonky modulo
					var hue = ((hash % 240) + 240)% 240;
					
					tournament.color = tinycolor({h: hue,s: 100,v: 50}).toHexString();					
				}

				return tournament.color;
			};

			$scope.newTournament = {
				name: '',
				customIdentifier: false,
				_identifier: undefined,

				identifier: function(identifier){
					if(angular.isDefined(identifier)){
						this._identifier = identifier;
					}
			
					if(!this.customIdentifier){
						this._identifier = this._defaultIdentifier();	
					}

					return this._identifier;
				},

				_defaultIdentifier: function() {
					var name = this.name || '';
					return name.toLowerCase().split(' ').join('-');	
				},
				customMemo: false,
				_memo: undefined,

				memo: function(memo){
					if(angular.isDefined(memo)){
						this._memo = memo;
					};

					if(!this.customMemo){
						this._memo = this._defaultMemo();
					}
					return this._memo;
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

				reset: function() {
					var defaultName = 'Basic Longsword Tournament';
					this.name = defaultName,
					this.customIdentifier = false;
					this._identifier = undefined;
					this.customMemo = false;
					this._memo = undefined;

					var i = 2;
					while($scope._findTournamentWithSameId(this)){
						this.name =  defaultName + " " + i++;
					}
				}
			};
			$scope.newTournament.reset();



			$scope.save = function() {
				if($scope._findTournamentWithSameId($scope.newTournament)){
					//TODO: Move this to validation directives
				} else {
					var t = $scope.newTournament;
					var tournament = new Tournament({
						name: t.name,
						identifier: t.identifier(),
						memo: t.memo(),
						participants: []
					});

					$scope.tournaments.push(tournament);
					$scope.newTournament.reset();

					tournament.$save();
					$scope.hideAddNewTournament();
				}
			};
		}])

		.controller('TournamentCtrl', ['$scope', '$routeParams', 'Tournament', function($scope, $routeParams, Tournament) {

		}])



	;

})();