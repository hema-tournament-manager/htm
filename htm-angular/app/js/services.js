'use strict';
(function(){
	/* Services */
	
	angular.module('htm.api', ['ngResource'])
		.factory('Tournament', ['$resource', function($resource){
			return $resource('/api/tournaments/:id', { "id" : "@id" }, 
				{ 
					update: { method: 'PUT' }
				}
			);
		}])


})();