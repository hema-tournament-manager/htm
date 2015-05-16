angular.module('htm.event').controller('EventPickerCtrl', function($scope, $meteor) {
  this.events = $meteor.collection(Events);

  var self = this;

  $meteor.autorun($scope, function() {
    self.active = $meteor.object(Events, Session.get('event'));
  });

  this.setActive = function(event) {
    Session.setPersistent('event', event._id);
  };
});