module.exports = function(config){
  config.set({

    basePath : '../',

    files : [

      'app/bower_components/angular/angular.js',
      'app/bower_components/angular-resource/angular-resource.js',
      'app/bower_components/angular-bootstrap/ui-bootstrap-tpls.js',
      'app/bower_components/angular-route/angular-route.js',
      'app/bower_components/angular-mocks/angular-mocks.js',

      'app/bower_components/jquery/dist/jquery.js',
      'app/bower_components/underscore/underscore.js',
      'app/bower_components/tinycolor/tinycolor.js',
      'bower_components/components-bootstrap/js/collapse.js',
      'bower_components/components-bootstrap/js/dropdown.js',

      'app/js/**/*.js',
      'test/unit/**/*.js'
    ],

    autoWatch : true,

    frameworks: ['jasmine'],

    browsers : ['Chrome'],

    plugins : [
            'karma-chrome-launcher',
            'karma-firefox-launcher',
            'karma-jasmine'
            ],

    junitReporter : {
      outputFile: 'test_out/unit.xml',
      suite: 'unit'
    }

  });
};