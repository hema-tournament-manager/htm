(function(){
	'use strict';

	/* Services */
	
	var api  = '/api/v3/';

	angular.module('htm.api', ['ngResource'])
	
		.factory('Tournament', ['$resource', 'Participant','Phase','Fight',
			function($resource,Participant,Phase,Fight){
				var Tournament = $resource(api + 'tournament/:id', { "id" : "@id" },{

						get: { 
							method: 'GET',
							isArray: false,
							transformResponse: function(Tournament){
								    
								angular.forEach(Tournament.participants, function(participant,index){
									Tournament.participants[index] = new Participant(participant);
								});

								angular.forEach(Tournament.phases, function(phase,index){
									Tournament.phases[index] = new Phase(phase);
								});

								angular.forEach(Tournament.fights, function(fight,index){
									Tournament.fights[index] = new Fight(fight);
								});

							    return Tournament;
							},
							headers:{'Content-Type': undefined}
						}});

			Tournament.prototype.getFights = function(fightIds){
				fightIds = fightIds || [];

				return _.filter(this.fights, function(fight){
					return _.contains(fightIds,fight.id);
				});
			};

			Tournament.prototype.getFightName = function(fightId){
					if(angular.isUndefined(fightId)){
						return ''
					}

					var fight =  _.find(this.fights, function(fight){
						return fight.id === fightId;
					});

					var phase = _.find(this.phases, function(phase){
						return phase.id === fight.phase;
					});

					if(angular.isUndefined(phase.pools)){
						return phase.name + " - " + fight.name;
					}

					var pool = _.find(phase.pools, function(pool){
						return _.contains(pool.fights,fight.id);
					});

					return phase.name + " - " + pool.name + " " + fight.name ;
			};
			
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

			Tournament.prototype.addFight = function(freestylePhase){
				var self = this;
				return freestylePhase.addFight().then(function(fight){
					self.fights.push(fight);
					return this;
				});
			}			

			return Tournament			

		}])
		.factory('Fight', ['$resource', function($resource) {
			return $resource(api + 'phase/:phase/fight/:id', { "phase" : "@phase", "id":"@id" });
		}])		
		.factory('Phase', ['$resource','Fight', function($resource,Fight) {
			var Phase = $resource(api + 'tournament/:id/phase', { "id" : "@id"});

			Phase.prototype.addFight = function(){
  				var self = this;

				return new Fight({phase:this.id}).$save().then(function(fight){
					self.fights.push(fight.id);
					return fight;
				});
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
						transformResponse: function(ParticipantList){

							angular.forEach(ParticipantList, function(participant,index){
								ParticipantList[index] = new Participant(participant);
							});

							return ParticipantList;
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