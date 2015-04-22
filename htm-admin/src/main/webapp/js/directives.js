(function(){
'use strict';

/* Directives */
	angular.module('htm.ui', [])
		
		/**
		 *	Usage:  <htm-navigation></htm-navigation>
		 *
		 *	Is replaced with the HTM top navigation
		 *
		 */
		.directive('htmNavigation', function() {
			return {
				restrict: 'E',
				replace: true,
				templateUrl:'/partials/navigation.html'
			};
		})

		/**
		 *	Usage:  <htm-tournament-memo=[data]></htm-tournament-memo>
		 *
		 *	Is replaced with the a tournament memo
		 *
		 */
		.directive('htmTournamentMemo', function() {
			return {
				restrict: 'E',
				replace: true,
				templateUrl:'/partials/tournament-memo.html',
				scope: { tournament: "="} ,
				controller: function($scope){

					$scope.color = function(tournament) {
						if(angular.isDefined(tournament.color)){
							return tournament.color;
						}

						var str = tournament.name || ''; 	

						var hash = 0;
						for (var i = 0; i < str.length; i++) {
							hash = str.charCodeAt(i) * 71 +  ((hash << 5) - hash);
						}
						// Work around for javascripts wonky modulo
						var hue = ((hash % 240) + 240)% 240;
						
						tournament.color = tinycolor({h: hue,s: 100,v: 50}).toHexString();					
						
						return tournament.color;
					};

				}
			};
		})

		/**
		 *	Usage:  <htm-tournament-list-item tournament=[data]></htm-tournament-list-item>
		 *
		 *	Is replaced the tournment list item bound to data.
		 *
		 */
		.directive('htmTournamentListItem', function() {
			return {
				restrict: 'E',
				replace: true,
				scope: {tournament:"="},
				templateUrl:'/partials/tournament-list-item.html',
			};
		})
		.directive('htmFlag', function() {
		  return {
		    restrict: 'E',
		    replace: true,
		    scope: {
		      country: '=',
		    },
		    templateUrl: '/partials/flag.html'
		  };
		})
		.directive('htmParticipantPhoto', function() {
		  return {
		    restrict: 'E',
		    replace: true,
		    scope: {
		      participant: '=',
		    },
		    templateUrl: '/partials/participant-photo.html'
		  };
		})
		.directive('htmSubscriptionLabel', function() {
		  return {
		    restrict: 'E',
		    replace: true,
		    scope: {
		      person: '=',
		      tournament: '='
		    },
		    controller: function($scope) {
		      $scope.subscription = _($scope.person.subscriptions).find(function(subscription) { 
		      	return subscription.tournament.id === $scope.tournament.id;
		      });
		    },
		    templateUrl: '/partials/participant-subscription-label.html'
		  };
		})
		/**
		 *	Usage: <ANY htm-focus-on=[binding]></ANY>
		 *
		 *	Will call focus to the atribute when the binding becomes truthy.
		 *
		 */
		.directive("htmFocusOn", ['$timeout', function($timeout) {
			return {
				restrict : 'A',
				link : function($scope,element,attr) {

					$scope.$watch(attr.htmFocusOn,function(_focusVal) {
						if(_focusVal){
							// Wrapped in timeout so event 
							// can trigger in new eval round 
							$timeout(function() {
								var selected = angular.element(element)[0];
								selected.focus();

								if(selected.select){
									selected.select();
								}
							});
						}
					 });
				}
			};
		}])
		.directive("htmInputFile", ['$parse',function($parse) {
			return {
				restrict : 'A',
				link : function($scope,element,attr) {

			     	var onChangeFun = $parse(attr.htmOnChange);
			     	var filesFun = $parse(attr.htmInputFile);
					
					$(element).on('change', function(event) {
						var element = this;
						$scope.$apply(function(){

							var files = angular.copy(element.files);

							files.toString = function(){
								return _.map(this,function(e){return e.name;}).join(', ');
							};

							filesFun.assign($scope,files);
						

							onChangeFun($scope)(files);
							


						});
					});
				}
			};
		}])
	.directive('htmFighterName', function() {
		return {
			restrict: 'E',
			replace: true,
			scope: {tournament:"=",fighter:"="},
			templateUrl:'/partials/fighter-name.html'
		};
	})
	.directive('htmFightPanel', function() {
		return {
			restrict: 'E',
			replace: true,
			scope: {fight:"=",tournament:"="},
			templateUrl:'/partials/fight-panel.html',
			controller: ['$scope','$modal', function($scope, $modal){

				$scope.editFight = function(fight){
		 		 	$modal.open({
						templateUrl: '/partials/fight-edit.html',
						controller: 'FightEditCtrl',
						size: 'sm',
						resolve: {
							fight: function () {
							  return fight;
							},
							tournaments: function() {
							  return $scope.tournament;
							}
						}
					});	
				}
				$scope.editFighter = function(fighter){
					
		 		 	$modal.open({
						templateUrl: '/partials/fighter-edit.html',
						controller: 'FighterEditCtrl',
						size: 'sm',
						resolve: {
							fighter: function () {
							  return fighter;
							},
							tournaments: function() {
							  return $scope.tournament;
							}
						}
					});				
				}


			}],
		};
	})
	.directive('htmLowerCase', function(){
		return {
			require: 'ngModel',
			restrict: 'A',
			type: 'input',
			link: function($scope, element, attrs, modelCtrl) {

				modelCtrl.$parsers.unshift(function (inputValue) {

					var transformed = inputValue.toLowerCase(); 

					if (transformed!=inputValue) {
						modelCtrl.$setViewValue(transformed);
						modelCtrl.$render();
					}         

					return transformed;         
				});
			}
		};
	})

	.directive('htmUpperCase', function(){
		return {
			require: 'ngModel',
			restrict: 'A',
			type: 'input',
			link: function($scope, element, attrs, modelCtrl) {

				modelCtrl.$parsers.unshift(function (inputValue) {

					var transformed = inputValue.toUpperCase(); 

					if (transformed!=inputValue) {
						modelCtrl.$setViewValue(transformed);
						modelCtrl.$render();
					}         

					return transformed;         
				});
			}
		};
	})

	.directive('htmReplaceSpace', function(){
		return {
			require: 'ngModel',
			restrict: 'A',
			type: 'input',
			scope: '@',
			link: function($scope, element, attrs, modelCtrl) {

				var replacement = attrs.htmNoSpace || ""; 

				modelCtrl.$parsers.unshift(function (inputValue) {

					var transformed = inputValue.replace(/\s/,replacement);

					if (transformed!=inputValue) {
						modelCtrl.$setViewValue(transformed);
						modelCtrl.$render();
					}         

					return transformed;         
				});
			}
		};
	})
	;

})();