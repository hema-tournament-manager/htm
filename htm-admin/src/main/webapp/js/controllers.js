(function(){
'use strict';

	angular.module('htm.welcome', [])

		.controller('WelcomeCtrl', ['$scope', function($scope) {
		}]);

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
			};
			
			$scope.isError = function(){
				return $scope.tournaments.$resolved && angular.isDefined($scope.tournaments.error);
			};

			$scope.isLoaded = function(){
				return $scope.tournaments.$resolved && angular.isUndefined($scope.tournaments.error);
			};
			
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

		}]);

	angular.module('htm.particpant', [])

		.controller('ParticipantListCtrl', ['$scope', '$modal','$routeParams','Tournament','Participant','Statistics',  function($scope,$modal,$routeParams,Tournament, Participant,Statistics) {
	
			var pages = [];

			$scope.searchCriteria = {
				page:0,
				items:15,
				query:undefined,
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
			}

			var debouncedRefresh = _.debounce(_refresh, 250);



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

			function registerSingleSelected(){
				var participants = $scope.participants();
				if(participants.length === 1){
					var participant = participants[0];
					participant.isPresent = true;
					participant.$save();
				}
			}

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


			/*
			 * Opens modal with the particpant and tournament promises
			 * this will delay opening of modal until they are resolved.
			 */
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
				      return $scope.tournaments.$promise;
				    }
				  }
				}).result;				
			}

		  	$scope.add = function(){
		  		openModal().then(function(newParticipant) {
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
  			};

  			/*
			 * Opens modal when particpants are viewed through /participant/:id
   			 */
			if($routeParams.participantId){
				var participant = Participant.get({id:$routeParams.participantId});
				openModal(participant.$promise).then(function(updatedParticipant){
					participants[updatedParticipant.id] = updatedParticipant;
				}).finally(function(){
					$scope.modalOpen=false;
	  			});
			}

		}])

		.controller('ParticipantRegistrationCtrl',function($scope, $modalInstance, Country, Participant, Club, participant, tournaments) {

			$scope.pictures = {files:[]};
			$scope.participant = participant || new Participant({club: {}, isPresent: false, previousWins: [], subscriptions: [], hasPicture: false });
			$scope.tournaments = _.filter(tournaments,function(tournament){
				return !_.find($scope.participant.subscriptions,function(subscription){
					return subscription.tournament.id === tournament.id;
				});
			});
			$scope.countries = Country.query();
			$scope.clubs = Club.query();

			$scope.addNewClubVisible = false;

			$scope.showAddNewClub = function(){
				$scope.oldClub = angular.copy($scope.participant.club);
				//Clean by individual property names so watchers see the update				
				$scope.participant.club.id = undefined;
				$scope.participant.club.name = undefined;
				$scope.participant.club.code = undefined;
				$scope.addNewClubVisible = true;
			};

			$scope.hideAddNewClub = function(){
				$scope.addNewClubVisible = false;
				//Restore by individual property names so watchers see the update
				$scope.participant.club.id = $scope.oldClub.id;
				$scope.participant.club.name = $scope.oldClub.name;
				$scope.participant.club.code = $scope.oldClub.code;
			};

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
   					  return item !== win;
				});
			};

			$scope.save = function() {

				$scope.participant.$save(function(savedParticipant){
					$modalInstance.close(savedParticipant);
				},function(error){
					//TODO: Handle error.
				});
			};

			$scope.checkin = function() {
				$scope.participant.isPresent = true;
				$scope.save();
			};

			$scope.cancel = function() {
				if(angular.isUndefined($scope.participant.id)){
					$modalInstance.dismiss('cancel');
					return;
				}

				$scope.participant.$get(function(refreshedParticipant){
					$modalInstance.dismiss('cancel');
				}, function(error){
					//TODO: Handle error.
				});

				
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