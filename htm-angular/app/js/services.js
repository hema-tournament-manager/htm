'use strict';
(function(){
	/* Services */
	
	var api  = 'http://localhost:8080/api/v3/';
	// var api  = '/api/';

	angular.module('htm.api', ['ngResource'])
	
		.factory('Tournament', ['$resource', function($resource){
			return $resource(api + 'tournament/:id', { "id" : "@id" }, 
				{ 
					update: { method: 'PUT' }
				}
			);
		}])
		.factory('Participant', ['$resource', function($resource){
			return $resource(api + 'participant/:id', { "id" : "@id" }, 
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
					}
				}
			);
		}])

		.factory('Country', ['$resource', function($resource) {
  			return $resource('/api/country');
		}])

		.factory('Club', ['$resource', function($resource) {
  			return $resource('/api/club');
		}])

		.factory('Statistics', ['$resource', function($resource) {
  			return $resource(api+'participant/totals');
		}])

})();