'use strict';
(function(){
	/* Services */
	
	angular.module('htm.api', ['ngResource'])
	
		.factory('Tournament', ['$resource', function($resource){
			return $resource('/api/tournament/:id', { "id" : "@id" }, 
				{ 
					update: { method: 'PUT' }
				}
			);
		}])
		.factory('Participant', ['$resource', function($resource){
			return $resource('/api/participant/:id', { "id" : "@id" }, 
				{ 
					update: { method: 'PUT' },
					postPicture: { 
						method: 'POST',
						params:{},
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

		//TODO: Doesn't have to be a service. Countries are enumerable.
		.factory('Country', ['$resource', function($resource) {
  			return $resource('/api/country/:id', { "id" : "@id" }, { update: { method: 'PUT' }});
		}])

})();