(function() {
'use strict';

    if(!document.URL.match(/\?nobackend/)) {
        return; // do nothing special - this app is not gonna use stubbed backend
    }

    console.log('======== ACHTUNG!!! USING STUBBED BACKEND ========');
    initializeStubbedBackend();

    function initializeStubbedBackend() {
        // 'codebrag' is our main application module
      angular.module('htm.App')            
        .config(function($provide) {
                $provide.decorator('$httpBackend', angular.mock.e2e.$httpBackendDecorator);
            })

            .run(function($httpBackend) {

                var clubs = [
                    {id: 0, code: "NW", name: "Noorderwind"},
                    {id: 1, code: "HTM", name: "H.T.M"},
                    {id: 2, code: "AMEK", name: "Academie voor Middeleeuwse Krijgskunst"}
                ];

                var countries = [
                {code2: "NL", name: "The Netherlands"},
                {code2: "DE", name: "Germany"}
                ];

                var participants = [{   id: 1,

                        name: 'Jack',
                        shortName: '0',
                        club: {id: 0, code: "NW", name: "Noorderwind"},
                        
                        country: {code2: "NL", name: "Netherlands"},
                        isPresent: true,
                        tshirt: 'String',
                        age: 27,
                        height: 188,
                        weight: 82,
                        previousWins: [ {text:'previousWins'},{text:'previousWins'}],

                        subscriptions: [],
                        hasPicture: false
                    },  {   id: 2,

                        name: 'Jones',
                        shortName: '1',
                        club: {id: 1, name:'H.T.M',code:'HTM'},
                        country: {code2: "NL", name: "Netherlands"},
                        isPresent: true,
                        tshirt: 'String',
                        age: 27,
                        height: 188,
                        weight: 82,
                        previousWins: [ {text:'previousWins'},{text:'previousWins'}],

                        subscriptions: [],
                        hasPicture: true
                    },{  id:  3,

                        name: 'Lumber',
                        shortName: '2',
                        club: {id: 1, name:'H.T.M',code:'HTM'},
                        
                        country: {code2: "NL", name: "Netherlands"},
                        isPresent: true,
                        tshirt: 'String',
                        age: 27,
                        height: 188,
                        weight: 82,
                        previousWins: [ {text:'previousWins'},{text:'previousWins'}],

                        subscriptions: [],
                        hasPicture: false
                    },{  id: 4,

                        name: 'Jack',
                        shortName: '3',
                        club: {id: 1, name:'H.T.M',code:'HTM'},
                        
                        country: {code2: "NL", name: "Netherlands"},
                        isPresent: true,
                        tshirt: 'String',
                        age: 27,
                        height: 188,
                        weight: 82,
                        previousWins: [ {text:'previousWins'},{text:'previousWins'}],

                        subscriptions: [],
                        hasPicture: false
                    },{  id: 4,

                        name: 'Had',
                        shortName: '4',
                        club: {id: 1, name:'H.T.M',code:'HTM'},
                        
                        country: {code2: "NL", name: "Netherlands"},
                        isPresent: true,
                        tshirt: 'String',
                        age: 27,
                        height: 188,
                        weight: 82,
                        previousWins: [ {text:'previousWins'},{text:'previousWins'}],

                        subscriptions: [],
                        hasPicture: false
                    },{  id: 5,

                        name: 'A',
                        shortName: '5',
                        club: {id: 2, code: "AMEK", name: "Academie voor Middeleeuwse Krijgskunst"},
                        
                        country: {code2: "NL", name: "Netherlands"},
                        isPresent: false,
                        tshirt: 'String',
                        age: 27,
                        height: 188,
                        weight: 82,
                        previousWins: [ {text:'previousWins'},{text:'previousWins'}],
                        subscriptions: [],
                        hasPicture: false
                    }];
  
                var tournaments = [
                {
                    id: 1,
                    name: "Longsword",
                    memo: "LS", 
                    participants: [],
                    phases: [
                    {
                        id: 1,
                        type: 'P', // Pool
                        name: "Pool Phase",
                        pools: [{
                            id:1, 
                            name: 'Swimming',                  
                            fights: [1,2]
                        }]
                    },{
                        id: 2,
                        type: 'F', // Freestyle
                        name: "Freestyle Phase",
                        fights: []
                    },{
                        id: 3,
                        name: "Semi-Finals",
                        type: 'E', // Elimination
                        fights: [3,4]
                    },{
                        id: 4,
                        name: "Finals",
                        type: 'E', // Elimination
                        fights: [5]
                    }
                ],
                fights: [{
                            id: 1,
                            phase: 1,
                            time: undefined,    //unscheduled
                            name: 'First Fight',
                            fighterA: {participant : 1},  // resolved future
                            fighterB: {},

                        },{
                            id: 2,
                            phase: 1,
                            time: undefined,    //unscheduled
                            name: 'Second Fight',
                            fighterA: {},
                            fighterB: {},
                        },{
                            id: 3,
                            phase: 3,
                            time: 1000, //scheduled
                            name: 'Third Fight',
                            fighterA: { winnerOf: 1},   // un resolved future
                            fighterB: { loserOf: 2},
                        },{
                            id: 4,
                            phase: 3,
                            time: 1000, //scheduled
                            name: 'Fourth Fight',
                            fighterA: { winnerOf: 2},   // un resolved future
                            fighterB: { loserOf: 1},
                        },{
                            id: 5,
                            phase: 4,
                            time: 1000, //scheduled
                            name: 'Fifth Fight',
                            fighterA: { winnerOf: 3},   // un resolved future
                            fighterB: { loserOf: 4},
                        }],
                },{
                    id: 2,
                    name: "Dagger",
                    memo: "DG", 
                    participants: [],
                    phases: [],
                }, {
                    id: 3,
                    name: "Sword and shield",
                    memo: "SAS", 
                    participants: [],
                    phases: [],
                }];


                $httpBackend.whenGET(/^\/partials\//).passThrough();

                $httpBackend.whenPOST('/api/v3/tournament').respond(function(method, url, data) {
                    console.log('tournament posted ' + data);

                    var tournament = angular.fromJson(data);
                    tournament.id = tournaments.length;
                    tournaments.push(tournament);
                    return [200, tournament];
                });
                $httpBackend.whenPOST(/\/api\/v3\/phase\/[0-9]+\/fight\/[0-9]+/).respond(function(method, url, data) {
                    console.log('fight posted ' + data);

                    var fight = angular.fromJson(data);
                    return [200,fight];
                });

                $httpBackend.whenPOST(/\/api\/v3\/phase\/[0-9]+\/fight/).respond(function(method, url, data) {
                    
                    var parts = url.split('/');
                    var id = parseInt(parts[parts.length-2]);

                    var fight = {
                            id: 999,
                            phase: id,
                            time: undefined,    //unscheduled
                            name: 'Posted Fight',
                            fighterA: {},
                            fighterB: {},
                        };
                    return [200,fight];
                });
                $httpBackend.whenGET(/\/api\/v3\/phase\/[0-9]+\/fight\/[0-9]+/).respond(function(method, url, data) {
                    
                    var parts = url.split('/');

                    var id = parseInt(parts[parts.length-1]);
                    
                
                    return [200,tournaments[0].fights[id-1]];
                });



                $httpBackend.whenGET(/\/api\/v3\/tournament\/[0-9]+/).respond(function(method, url, data) {
                    var parts = url.split('/');
                    var id = parseInt(parts[parts.length-1]);

                    return [200,tournaments[id-1]];
                });
                $httpBackend.whenGET('/api/v3/tournament').respond(tournaments);


                $httpBackend.whenGET(/\/api\/v3\/participant\/[0-9]+/).respond(function(method, url, data) {
                    var parts = url.split('/');
                    var id = parseInt(parts[parts.length-1]);
                    return [200,participants[id-1]];
                });

                $httpBackend.whenPOST(/\/api\/v3\/participant\/picture\/[0-9]+/).respond(function(method, url, data) {
                    console.log('participant picture posted ' + data);

                    var parts = url.split('/');
                    var id = parseInt(parts[parts.length-1]);
                    participants[id-1].hasPicture=true;
                    var participant = {id:id,hasPicture:true};

                    return [200,participant];
                });

                $httpBackend.whenPOST('/api/v3/participant/picture').respond(function(method, url, data) {
                    console.log('new participant picture posted ' + data);
                    var participant = {id:participants.length,hasPicture:true};
                    participants.push(participant);
                    return [200,participant];
                });

                $httpBackend.whenPOST(/\/api\/v3\/participant\/[0-9]+\/subscribe\/[0-9]+/).respond(function(method, url, data) {
                    console.log('participant subscribed ' + data);

                    var subscription = angular.fromJson(data);
                    var participant = participants[subscription.participant-1]
                    participant.subscriptions.push(subscription);
                    subscription.fighterNumber = 999;
                    return [200,subscription];
                });

                $httpBackend.whenPOST(/\/api\/v3\/participant\/[0-9]+/).respond(function(method, url, data) {
                    console.log('participant posted ' + data);

                    var participant = angular.fromJson(data);
                    participants[participant.id] = participant;

                    var club = participant.club;
                    if(angular.isUndefined(club.id)){
                        club.id = clubs.length;
                        clubs.push(angular.copy(club));
                    }

                    return [200,participant];
                });


                $httpBackend.whenPOST('/api/v3/participant').respond(function(method, url, data) {
                    
                    console.log('new participant posted ' + data);
                    var participant = angular.fromJson(data);


                    participant.id = participants.length;

                    var club = participant.club;
                    if(angular.isUndefined(club.id)){
                        club.id = clubs.length;
                        clubs.push(angular.copy(club));
                    }

                    participants.push(participant);
                    return [200, participant];
                });

                $httpBackend.whenGET('/api/v3/participant/totals').respond(function(method, url, data){

                    var totals = {
                        participants: participants.length,
                        clubs: clubs.length,
                        countries: countries.length
                    };
                    
                    return [200,totals];
                });

                $httpBackend.whenGET(/\/api\/v3\/participant.*/).respond(function(method,url,data,headers){
                    console.log('participant queried ' + url);

                    return [200,participants];
                });

                $httpBackend.whenGET('/api/v3/country').respond(countries);

                $httpBackend.whenGET('/api/v3/club').respond(function(method,url,data,headers){
                    console.log('clus queried ' + url);

                    return [200,clubs];
                });



        });

    }
})();