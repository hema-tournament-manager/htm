/*
  Javascript unit test configuration for development 
  use. Watches a directory when started and executes
  tests when needed.

  For build system and CI specific use karma.conf.ci.js.
*/

(function(){
'use strict';

  module.exports = function(config){
    config.set({


      files : [

        'src/main/webapp/bower_components/angular/angular.js',
        'src/main/webapp/bower_components/angular-resource/angular-resource.js',
        'src/main/webapp/bower_components/angular-bootstrap/ui-bootstrap-tpls.js',
        'src/main/webapp/bower_components/angular-route/angular-route.js',
        'src/main/webapp/bower_components/angular-mocks/angular-mocks.js',

        'src/main/webapp/bower_components/jquery/dist/jquery.js',
        'src/main/webapp/bower_components/underscore/underscore.js',
        'src/main/webapp/bower_components/tinycolor/tinycolor.js',
        'src/main/webapp/bower_components/components-bootstrap/js/collapse.js',
        'src/main/webapp/bower_components/components-bootstrap/js/dropdown.js',

        'src/main/webapp/js/**/*.js',
        'src/test/javascript/unit/**/*.js'
      ],

      autoWatch : true,

      frameworks: ['jasmine'],

      browsers : ['PhantomJS'],

      plugins : [
              'karma-phantomjs-launcher',
              'karma-jasmine'
              ],

      junitReporter : {
        outputFile: 'target/test_out/unit.xml',
        suite: 'unit'
      }

    });
  };

})();