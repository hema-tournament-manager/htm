(function(){
'use strict';

	module.exports = function(grunt) {

		grunt.loadNpmTasks('grunt-contrib-jshint');
		grunt.loadNpmTasks('grunt-karma');
		
		// Project configuration.
		grunt.initConfig({
			pkg: grunt.file.readJSON('package.json'),
			
			jshint: {
				options: {
	            	jshintrc: '.jshintrc'
	        	},

				all: [	'karma.conf.js',
						'karma.conf.ci.js',
						'gruntfile.js', 
						'src/main/webapp/js/**/*.js',
						'src/test/javascript/**/*.js']
			},

			karma: {
			  unit: {
			    configFile: 'karma.conf.js'
			  }
			}
		});

		
		grunt.registerTask('test', ['karma','jshint']);

		// Called during the Maven generate-resource phase
		grunt.registerTask('maven', []);


	};

})();	