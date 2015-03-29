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

		.controller('ParticipantListCtrl', ['$scope', '$modal','$routeParams','Tournament','Participant','Statistics',  function($scope,$modal,$routeParams,Tournament, Participant,Statistics) {
	
			$scope.ORDER = {ASC:'ASC',DESC:'DESC'};
			$scope.ORDER_BY = {NAME:'name',SHORT_NAME:'shortName',CLUB:'club',CLUB_CODE:'clubCode'};
			var pages = [];

			$scope.searchCriteria = {
				page:0,
				itemsPerPage:15,
				query:undefined,
				orderBy: $scope.ORDER_BY.NAME,
				order: $scope.ORDER.ASC,
			};

			pages[0] = Participant.query($scope.searchCriteria);

			$scope.totals = Statistics.get();
			$scope.tournaments = Tournament.query();
			$scope.modalOpen = false;

			function _refresh(){
				$scope.searchCriteria.page = 0;
				Participant.query($scope.searchCriteria).$promise.then(function(freshParticipants){
					pages = [];
					pages[0] = freshParticipants;
				}, function(error){
					//TODO: Handle error
				});
			};

			var debouncedRefresh = _.debounce(_refresh, 250);

			function registerSingleSelected(){
				var participants = $scope.participants();
				if(participants.length === 1){
					var participant = participants[0];
					participant.isPresent = true;
					participant.$save();
				}
			};

			$scope.refresh = function($event){
				if(event.keyCode === 13){
					registerSingleSelected();
				} else {
					debouncedRefresh();
				}
			};

			$scope.more = function(){
				var newPage = ++$scope.searchCriteria.page;
				Participant.query($scope.searchCriteria)
					.$promise.then(function(freshParticipants){
						pages[newPage] = freshParticipants;
					});
			};

			$scope.participants = function(){
				return _.flatten(pages);
			};

			$scope.registerSelected = function(){
				angular.forEach($scope.participants(), function(participant){
					participant.isPresent = true;
					participant.$save();

				});
			};

			$scope.unregisterSelected = function(){
				angular.forEach($scope.participants(), function(participant){
					participant.isPresent = false;
					participant.$save();

				});
			};

			$scope.sort = function(fieldName){
				var criteria = $scope.searchCriteria;
				var ORDER = $scope.ORDER;

				if(criteria.orderBy === fieldName){
					criteria.order = criteria.order === ORDER.ASC ? ORDER.DESC : ORDER.ASC;
				} else {
					criteria.order = ORDER.ASC;
					criteria.orderBy = fieldName;
				}

				debouncedRefresh();
			};


			function openModal(participant) {
				$scope.modalOpen=true;
				
				return $modal.open({
				  templateUrl: '/partials/participant-registration.html',
				  controller: 'ParticipantRegistrationCtrl',
				  size: 'lg',
				  resolve: {
				    participant: function () {
				      return participant;
				    },
				    tournaments: function() {
				      return $scope.tournaments;
				    }
				  }
				}).result;				
			};

		  	$scope.add = function(){
		  		openModal(new Participant({ 
						country: {},
						isPresent: false,
						previousWins: [ ],
						subscriptions: [],
						hasPicture: false
					})).then(function(newParticipant) {
		  				pages[0].unshift(newParticipant);
						$scope.totals = Statistics.get();
		  			}).finally(function(){
						$scope.modalOpen=false;
		  			});
			};

		  	$scope.hasDetails = function(participant) {
		    	return participant.age || participant.height || participant.weight;
  			};

  			$scope.show = function(participant){
  				openModal(participant).finally(function(){
					$scope.modalOpen=false;
	  			});
  			}


			if($routeParams.participantId){
				var participant = Participant.get({id:$routeParams.participantId});
				openModal(participant).then(function(updatedParticipant){
					participants[updatedParticipant.id] = updatedParticipant;
				}).finally(function(){
					$scope.modalOpen=false;
	  			});
			}

		}])

		.controller('ParticipantRegistrationCtrl',function($scope, $modalInstance, Country, Participant, participant, tournaments) {

			$scope.pictures = {files:[]};
			$scope.participant = participant;
			$scope.tournaments = _.filter(tournaments,function(tournament){
				return !_.find(participant.subscriptions,function(subscription){
					return subscription.tournament.id === tournament.id;
				})
			});
			$scope.countries = Country.query();

			$scope.addWin = function() {
				if ($scope.canAddWin()) {
				  $scope.participant.previousWins.push({text: ''});
				}
			};

			$scope.canAddWin = function() {
				return $scope.participant.previousWins.length < 3;
			};

			$scope.removeWin = function(win) {
				$scope.participant.previousWins = _($scope.participant.previousWins).filter(function(item) {
   					  return item !== win
				});
			};

			$scope.save = function() {
				$scope.participant.$save(function(savedParticipant){
					$modalInstance.close(savedParticipant);
				});
			};

			$scope.checkin = function() {
				$scope.participant.isPresent = true;
				$scope.save();
			};

			$scope.cancel = function() {
				if($scope.participant.id){
					$scope.participant.$get();
				}
				
				$modalInstance.dismiss('cancel');
			};

			$scope.upload = function(pictures){
				var picture = new Participant({id:$scope.participant.id,file:pictures[0]});
				picture.$postPicture().then(function(participant){
					$scope.participant.id = participant.id;
					$scope.participant.hasPicture = participant.hasPicture;
				},function(error){
					// TODO: Handle error
				});
			};


		})
	;

})();