(function() {
'use strict';

    if(!document.URL.match(/\?nobackend$/)) {
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

                var tournaments = [
                {
                    id: 1,
                    name: "Longsword",
                    memo: "LS", 
                    participants: [1,2,3,4,5],
                },{
                    id: 2,
                    name: "Dagger",
                    memo: "DG", 
                    participants: [1,2,3],
                }, {
                    id: 3,
                    name: "Sword and shield",
                    memo: "SAS", 
                    participants: [],
                }];

                var participants = [{   id: 0,

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

                        subscriptions: [{
                            fighterNumber: 1,
                            gearChecked: true,
                            droppedOut: true,
                            pool: 'Swimming',
                            tournament: {
                                id: 1, name: 'Dagger', memo: 'DG'
                            }
                        },{
                            fighterNumber: 1,
                            gearChecked: false,
                            droppedOut: false,
                            pool: 'Swimming',
                            tournament: {
                                id: 2, name: 'Longsword', memo: 'LS'
                            }
                        }],
                        hasPicture: false
                    },  {   id: 1,

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

                        subscriptions: [{
                            fighterNumber: 1,
                            gearChecked: true,
                            droppedOut: true,
                            pool: 'Swimming',
                            tournament: {
                                id: 1, name: 'Dagger', memo: 'DG'
                            }
                        },{
                            fighterNumber: 1,
                            gearChecked: false,
                            droppedOut: false,
                            pool: 'Swimming',
                            tournament: {
                                id: 2, name: 'Longsword', memo: 'LS'
                            }
                        }],
                        hasPicture: true
                    },{  id:  2,

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

                        subscriptions: [{
                            fighterNumber: 2,
                            gearChecked: true,
                            droppedOut: false,
                            pool: 'Swimming',
                            tournament: {
                                id: 1, name: 'Longsword', memo: 'LS'
                            }
                        },{
                            fighterNumber: 2,
                            gearChecked: false,
                            droppedOut: false,
                            pool: 'Swimming',
                            tournament: {
                                id: 2, name: 'Longsword', memo: 'LS'
                            }
                        }],
                        hasPicture: false
                    },{  id: 3,

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
                        fighterNumber: 'fighterNumber',

                        subscriptions: [{
                            fighterNumber: 3,
                            gearChecked: false,
                            droppedOut: false,
                            pool: 'Swimming',
                            tournament: {
                                id: 1, name: 'Longsword', memo: 'LS'
                            }
                        },{
                            fighterNumber: 3,
                            gearChecked: false,
                            droppedOut: false,
                            pool: 'Swimming',
                            tournament: {
                                id: 2, name: 'Longsword', memo: 'LS'
                            }
                        }],
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
                        fighterNumber: 'fighterNumber',

                        subscriptions: [{
                            fighterNumber: 4,
                            gearChecked: true,
                            droppedOut: true,
                            pool: 'Swimming',
                            tournament: {
                                id: 1, name: 'Longsword', memo: 'LS'
                            }
                        }],
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
                        subscriptions: [{
                            fighterNumber: 5,
                            gearChecked: false,
                            droppedOut: false,
                            pool: 'Swimming',
                            tournament: {
                                id: 1, name: 'Longsword', memo: 'LS'
                            }
                        }],
                        hasPicture: false
                    }];


                $httpBackend.whenGET(/^\/partials\//).passThrough();

                $httpBackend.whenGET('/api/v3/tournament').respond(tournaments);
                $httpBackend.whenPOST('/api/v3/tournament').respond(function(method, url, data) {
                    console.log('tournament posted ' + data);

                    var tournament = angular.fromJson(data);
                    tournament.id = tournaments.length;
                    tournaments.push(tournament);
                    return [200, tournament, {}];
                });

                $httpBackend.whenGET(/\/api\/v3\/participant\/[0-9]+/).respond(function(method, url, data) {
                    var parts = url.split('/');
                    var id = parseInt(parts[parts.length-1]);
                    return [200,participants[id]];
                });

                $httpBackend.whenPOST(/\/api\/v3\/participant\/picture\/[0-9]+/).respond(function(method, url, data) {
                    console.log('participant picture posted ' + data);

                    var parts = url.split('/');
                    var id = parseInt(parts[parts.length-1]);
                    participants[id].hasPicture=true;
                    var participant = {id:id,hasPicture:true};

                    return [200,participant];
                });

                $httpBackend.whenPOST('/api/v3/v3/participant/picture').respond(function(method, url, data) {
                    console.log('new participant picture posted ' + data);
                    var participant = {id:participants.length,hasPicture:true};
                    participants.push(participant);
                    return [200,participant];
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

                    return [200];
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
                    return [200, participant, {}];
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