'use strict';

angular.module('htm', ['common.playRoutes', 'common.filters', 'htm.services', 'ui.bootstrap']).
controller('BattleCtrl', ['$rootScope', '$scope', '$timeout', '$modal', '$location', '$filter', 'playRoutes', 'appService', BattleCtrl]).
controller('PoolsCtrl', ['$rootScope', '$scope', '$timeout', '$location', 'playRoutes', 'appService', PoolsCtrl]).config(
		[ '$routeProvider', function($routeProvider) {
			$routeProvider.when('/arenas', {
				templateUrl : 'static/battle/templates/arenas.html',
				controller : 'PoolsCtrl'
			}).when('/fight', {
				templateUrl : 'static/battle/templates/fight.html',
				controller : 'BattleCtrl'
			}).otherwise({
				redirectTo : '/arenas'
			});
		} ]).run([
		  "$rootScope",
		  function ($rootScope) {
			$rootScope.chat = {
					closed: true,
					state: function() {
						return this.closed ? "closed" : "opened"
					},
					toggle: function() {
						this.closed = !this.closed;
						if (!this.closed) {
							this.unread = 0;
							$("#chatbox input").focus();
						}
					},
					messages: new Array(),
					addMessage: function(message) {
						if (this.closed) {
							this.unread += 1;
						}
						this.messages.push(message);
					},
					unread: 0};
		    
		    $(document).on('new-chat-message', function(event, data) {
		    	$rootScope.$apply(function() {
		    		$rootScope.chat.addMessage(data);
		    	});
		    	$("#chatbox .messages").animate({ scrollTop: $('#chatbox .messages')[0].scrollHeight}, 350);
		    }).on('initial-chat-messages', function(event, data) {
		      //We do this to get the array as one var.
		      var messages =  Array.prototype.slice.call(arguments, 1);
		      /**
		       * If you open a new tab, Lift will send you all stored messages, so we
		       * avoid duplicating them here
		       */
		      $rootScope.$apply(function() {
			      if ($rootScope.chat.messages.length > 0) {
			        $.each(messages, function(index, value) {
			        	$rootScope.chat.addMessage(value);
			        });
			      }
		      });
		    });
		}]);
