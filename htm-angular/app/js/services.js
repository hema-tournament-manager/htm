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
					update: { method: 'PUT' }
				}
			);
		}])

		.factory('Country', ['$resource', function($resource) {
  			return $resource('/api/country/:id', { "id" : "@id" }, { update: { method: 'PUT' }});
		}])

})();