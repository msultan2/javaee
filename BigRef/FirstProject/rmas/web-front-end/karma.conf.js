module.exports = function (config) {
    config
            .set({
                basePath: './',
                files: [
                    'target/gulp-dev-output/js/0.vendor.js',
                    'target/gulp-dev-output/js/**/*.js',
                    'target/mergedSrc/test/frontend/**/*.js'],
                logLevel: config.LOG_DEBUG,
                autoWatch: false,
                singleRun: true,
                frameworks: ['jasmine'],
                browsers: ['PhantomJS'],
                plugins: ['karma-chrome-launcher', 'karma-firefox-launcher', 'karma-phantomjs-launcher',
                    'karma-jasmine', 'karma-junit-reporter', 'karma-coverage'],
                preprocessors: {
                    'target/gulp-dev-output/js/**/!(0.vendor).js': ['coverage']
                },
                reporters: ['dots', 'junit', 'coverage'],
                junitReporter: {
                    outputFile: 'target/surefire-reports/TEST-karma-results.xml'
                },
                coverageReporter: {
                    dir: 'target/coverage/',
                    reporters: [
                        {type: 'html', subdir: 'report-html'},
                        {type: 'cobertura', subdir: '.', file: 'cobertura.xml'}
                    ]
                }
            });
};
