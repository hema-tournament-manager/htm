Meteor.startup(function() {
  if (Tournaments.find().count() === 0) {
    var tournaments = [
      {
        name: 'Open Longsword',
        identifier: 'open-longsword',
        memo: 'LS'
      },
      {
        name: 'Sabre',
        identifier: 'sabre',
        memo: 'S'
      }
    ];

    tournaments.forEach(function(tournament) {
      Tournaments.insert(tournament);
    });
  }
});