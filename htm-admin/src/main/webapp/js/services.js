(function(){
	'use strict';

	/* Services */
	
	var api  = '/api/v3/';

	angular.module('htm.api', ['ngResource'])
	
		.factory('Tournament', ['$resource', 'Participant','Phase','Fight',
			function($resource,Participant,Phase,Fight){

				function transformResponse(response){
					var tournament = angular.fromJson(response);

					angular.forEach(tournament.participants, function(participant,index){
						tournament.participants[index] = new Participant(participant);
					});

					angular.forEach(tournament.phases, function(phase,index){
						var phase = new Phase(phase);

						angular.forEach(phase.fights, function(fight,index){
							phase.fights[index] = new Fight(fight);
						});

						tournament.phases[index] = phase;
					});

				    return tournament;
				}

				var Tournament = $resource(api + 'tournament/:id', { "id" : "@id" },{
						get: { 
							method: 'GET',
							isArray: false,
							transformResponse: transformResponse,
						
						}, 
						generateElimination: { 
							method: 'GET',
							isArray: false,
							url: api + 'tournament/:id/generate/elimination/',
							transformResponse: transformResponse,
						}, 
						generatePools: { 
							method: 'GET',
							isArray: false,
							url: api + 'tournament/:id/generate/pools/',
							transformResponse: transformResponse,
						},							
				});
				
				Tournament.prototype.getParticipant = function(participantId){
					return _.find(this.participants, function(participant){
						return participant.id === participantId;
					});
				};

				Tournament.prototype.subscribe = function(participant){
					var self = this;
					return participant.subscribe(this.id).then(function(subscription){
						self.participants.push(participant);
						return this;
					});
				};

				Tournament.prototype.isSubscribed = function(participant){
					return _.some(this.participants, function(tParticipant){
						return tParticipant.id === participant.id;
					});
				}
				Tournament.prototype.getFights = function(){
					return _.flatten(_.pluck(this.phases,'fights'));	
				}	
				Tournament.prototype.getFight = function(ofFight){
					return _.findWhere(_.flatten(_.pluck(this.phases,'fights')),ofFight);	
				}	

				Tournament.prototype.getPools = function(){
					return _.compact(_.flatten(_.pluck(this.phases,'pools')));
				}	

				Tournament.prototype.getPool = function(poolId){
					return _.findWhere(this.getPools(),{id:poolId});
				}		

				return Tournament			

		}])
		.factory('Fight', ['$resource', function($resource) {
			var Fight =  $resource(api + 'phase/:phaseType/:phase/fight/:id', { "phaseType" : "@phaseType", "phase":"@phase", "id":"@id"});

			Fight.prototype.isPool = function(){
				return this.phaseType === 'P'
			}

			Fight.prototype.isFreestyle = function(){
				return this.phaseType === 'F'
			}

			Fight.prototype.isElimination = function(){
				return this.phaseType === 'E'
			}

			Fight.prototype.equals = function(fight){
				return this.id === fight.id && this.phaseType === fight.phaseType;
			}

			return Fight;
		}])		
		.factory('Phase', ['$resource','Fight', function($resource,Fight) {
			var Phase = $resource(api + '/phase/:phaseType/:id', { "phaseType":"@phaseType", "id" : "@id"});

			Phase.prototype.addFight = function(){
  				var self = this;

				return new Fight({phaseType: this.phaseType, phase:this.id}).$save().then(function(fight){
					self.fights.push(fight.id);
					return fight;
				});
			}

			Phase.prototype.getFights = function(fightIds){
				fightIds = fightIds || [];

				return _.filter(this.fights, function(fight){
					return _.contains(fightIds,fight.id);
				});
			};

			Phase.prototype.isPool = function(){
				return this.phaseType === 'P'
			}

			Phase.prototype.isFreestyle = function(){
				return this.phaseType === 'F'
			}

			Phase.prototype.isElimination = function(){
				return this.phaseType === 'E'
			}

			return Phase;
		}])
		.factory('Participant', ['$resource', 'Subscription', function($resource, Subscription){
			var Participant = $resource(api + 'participant/:id', { "id" : "@id" }, 
				{ 
					update: { method: 'PUT' },
					postPicture: { 
						method: 'POST',
						params:{},
						url: api+'participant/picture/:id',
						transformRequest: function(data){
							    var fd = new FormData();
							   	fd.append('file',data.file);
							    return fd;
						},
						headers:{'Content-Type': undefined}
					}, 

					query: { 
						method: 'GET',
						isArray: true,
						transformResponse: function(response){
							var participantList = angular.fromJson(response);

							angular.forEach(participantList, function(participant,index){
								participantList[index] = new Participant(participant);
							});

							return participantList;
						},
					}
				}
			);

		  	Participant.prototype.hasDetails = function(participant) {
		    	return this.age || this.height || this.weight;
  			};

		  	Participant.prototype.getSubscription = function(tournamentId) {
		    	return _.find(this.subscriptions,function(subscription){
		    		return subscription.tournament === tournamentId;
		    	});
  			};

  			/**
  				Subscribes a participant to a tournament
  			*/
			Participant.prototype.subscribe = function(tournamentId){

  				var subscription = new Subscription({ participant: this.id,
  													  tournament: tournamentId, 
										 			  gearChecked: false,
                            			  			  droppedOut: false})
  				var self = this;
  				return subscription.$save().then(function(subscription){
  					self.subscriptions.push(subscription);
  					return subscription;
  				});
  			}

  			return Participant;
		}])

		.factory('Subscription', ['$resource', function($resource) {
  			return $resource(api+'participant/:participant/subscribe/:tournament',{ "participant" : "@participant","tournament" : "@tournament" });
		}])

		.factory('Country', ['$resource', function($resource) {
  			return $resource(api + 'country');
		}])

		.factory('Club', ['$resource', function($resource) {
  			return $resource(api + 'club');
		}])

		.factory('Statistics', ['$resource', function($resource) {
  			return $resource(api+'participant/totals');
		}])

		;

})();