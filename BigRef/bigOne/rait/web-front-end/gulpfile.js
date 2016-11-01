/*
 * gulpfile.js
 *
 *
 * THIS SOFTWARE IS PROVIDED BY SIMULATION SYSTEMS LTD ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL SIMULATION
 * SYSTEMS LTD BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2016 (C) Simulation Systems Ltd. All Rights Reserved.
 *
 */

var gulp = require('gulp'),
    plumber = require('gulp-plumber'),
    ngAnnotate = require('gulp-ng-annotate'),
    uglify = require('gulp-uglify'),
    sourceMaps = require('gulp-sourcemaps'),
    concat = require('gulp-concat'),
    imageMin = require('gulp-imagemin'),
    htmlMin = require('gulp-html-minifier'),
    cssMin = require('gulp-minify-css'),
    jsHint = require('gulp-jshint'),
    less = require('gulp-less'),
    template = require('gulp-template'),
    bower = require('gulp-bower'),
    data = require('gulp-data'),
    fs = require('fs'),
    path = require('path'),
    util = require('util');
    ngConstant = require('gulp-ng-constant'),
    merge = require('merge-stream'),
    newer = require('gulp-newer'),
    filter = require('gulp-filter');

var template_data_path = './src/main/config/config.json';
var template_data = require(template_data_path);

var vendor_js_sources = [
    "bower_components/angular/angular.js",
    "bower_components/angular-route/angular-route.js",
    "bower_components/d3/d3.js",
    "bower_components/nvd3/nv.d3.js",
    "bower_components/angularjs-nvd3-directives/dist/angularjs-nvd3-directives.js",
    "bower_components/angular-bootstrap/ui-bootstrap-tpls.js",
    "bower_components/angular-route/angular-route.js",
    "bower_components/angular-mocks/angular-mocks.js",
    "bower_components/angular-jwt/dist/angular-jwt.js",
    "bower_components/ngstorage/ngStorage.js",
    "bower_components/ng-file-upload/ng-file-upload.js",
    "bower_components/angular-base64/angular-base64.js"
];

var srcBase = 'target/mergedSrc';

var pre_compile_tasks = [];
var compile_tasks = [];

function pre_compile_task(name, func) {
    pre_compile_tasks.push(name);
    gulp.task(name, func);
}

function compile_task(name, func) {
    compile_tasks.push(name);
    gulp.task(name, pre_compile_tasks, func);
}

pre_compile_task('bower', bower);

function failBuild(err) {
    console.log(err.toString());
    process.exit(1);
}

compile_task('dev-compile-css', function () {
    return gulp.src([srcBase + '/main/less/**/*.less'])
        .pipe(plumber())
        .pipe(newer('target/gulp-dev-output/css/style.css'))
        .pipe(filter('style.less'))
        .pipe(sourceMaps.init())
            .pipe(less())
            .on('error', failBuild)
        .pipe(sourceMaps.write('maps'))
        .pipe(gulp.dest('target/gulp-dev-output/css'));
});

compile_task('compile-css', function () {
    return gulp.src([srcBase + '/main/less/**/*.less'])
        .pipe(plumber())
        .pipe(newer('target/gulp-dev-output/css/style.css'))
        .pipe(filter('style.less'))
        .pipe(sourceMaps.init())
            .pipe(less())
            .on('error', failBuild)
            .pipe(cssMin({
                keepSpecialComments: "*"
            }))
        .pipe(sourceMaps.write('maps'))
        .pipe(gulp.dest('target/gulp-output/css'));
});

compile_task('dev-minify-images', function () {
    return gulp.src(srcBase + '/main/images/**/*')
        .pipe(plumber())
        .pipe(newer('target/gulp-dev-output/images'))
        .pipe(gulp.dest('target/gulp-dev-output/images'));
});

compile_task('minify-images', function () {
    return gulp.src(srcBase + '/main/images/**/*')
        .pipe(plumber())
        .pipe(newer('target/gulp-output/images'))
        .pipe(imageMin({
            svgoPlugins: [
                {cleanupIDs: false}
            ]
        }))
        .pipe(gulp.dest('target/gulp-output/images'));
});

compile_task('dev-compile-vendor-js', function () {
    return gulp.src(vendor_js_sources)
        .pipe(plumber())
        .pipe(newer('target/gulp-dev-output/js/0.vendor.js'))
        .pipe(concat('0.vendor.js'))
        .pipe(gulp.dest('target/gulp-dev-output/js'));
});

compile_task('compile-vendor-js', function () {
    return gulp.src(vendor_js_sources)
        .pipe(plumber())
        .pipe(newer('target/gulp-output/js/vendor.min.js'))
        .pipe(ngAnnotate())
        .pipe(concat('vendor.min.js'))
        .pipe(uglify())
        .pipe(gulp.dest('target/gulp-output/js'));
});

function getStreamWithConstants() {
    return gulp.src(template_data_path)
        .pipe(plumber())
        .pipe(ngConstant({name: 'constants'}));
}

function getJSDevBuildPipeline(failFast) {
    var plumberedSource = gulp.src(srcBase + '/main/js/**/*.js')
        .pipe(plumber())
        .pipe(jsHint({browser:true}))
        .pipe(jsHint.reporter('jshint-stylish'));
    var mergedSource = merge(plumberedSource, getStreamWithConstants());
    if (!failFast) {
        mergedSource = mergedSource.pipe(plumber());
    }
    mergedSource = mergedSource.pipe(newer('target/gulp-dev-output/js'));
    if (failFast) {
        mergedSource = mergedSource.pipe(plumber.stop());
    }

    return mergedSource.pipe(jsHint.reporter('fail'))
        .pipe(gulp.dest('target/gulp-dev-output/js'));
}

compile_task('dev-compile-js', function () {
    return getJSDevBuildPipeline(true);
});

gulp.task('dev-compile-js-safe', function () {
    return getJSDevBuildPipeline(false);
});

compile_task('compile-js', function () {
    var plumberedSource = gulp.src(srcBase + '/main/js/**/*.js')
        .pipe(plumber())
        .pipe(jsHint({browser:true}))
        .pipe(jsHint.reporter('jshint-stylish'));
    return merge(plumberedSource, getStreamWithConstants())
        .pipe(newer('target/gulp-output/js/bundle.min.js'))
        .pipe(sourceMaps.init())
            .pipe(ngAnnotate())
            .pipe(concat('bundle.min.js'))
            .pipe(uglify())
        .pipe(sourceMaps.write('maps'))
        .pipe(plumber.stop())
        .pipe(jsHint.reporter('fail'))
        .pipe(gulp.dest('target/gulp-output/js'));
});

compile_task('lint-test-js', function () {
    return gulp.src(srcBase + '/test/frontend/**/*.js')
        .pipe(plumber())
        .pipe(jsHint({
			jasmine: true,
			unused: "vars",
			globals: {
				console: false,
				module: false,
				inject: false,
				angular: false
			},
                        browser:true
		}))
        .pipe(jsHint.reporter('jshint-stylish'))
        .pipe(plumber.stop())
        .pipe(jsHint.reporter('fail'));
});

function walk(currentDirPath, extension, fromPath) {
    var results = [];
    fs.readdirSync(currentDirPath).forEach(function(name) {
        var filePath = path.join(currentDirPath, name);
        var stat = fs.statSync(filePath);
        if (stat.isFile() && (filePath.indexOf(extension, filePath.length - extension.length) !== -1)) {
            results.push(path.relative(fromPath, filePath));
        } else if (stat.isDirectory()) {
            results = results.concat(walk(filePath, extension, fromPath));
        }
    });
    return results;
}

function paths_to_script_tag_block(includes, base_path) {
    return includes.map(function(source_path) {
            return '<script type="text/javascript" src="' + base_path + source_path + '"></script>\n';
        })
        .reduce(function(prev, current) {
            return prev + current;
        });
}

function paths_to_style_block(includes, base_path) {
    return includes.map(function(source_path) {
            return '<link rel="stylesheet" href="' + base_path + source_path + '"/>\n';
        })
        .reduce(function(prev, current) {
            return prev + current;
        });
}

function calculate_relative_path(source_path, file_path) {
    var basePath = path.relative(path.dirname(file_path), source_path);
    if (basePath.length > 0) {
        basePath += "/";
    }
    return basePath;
}

function getHTMLBuildPipeline() {
    var js_includes = walk('target/gulp-dev-output/js', '.js', 'target/gulp-dev-output');
    var css_includes = ["css/style.css"];

    return gulp.src(srcBase + '/main/html/**/*.html')
        .pipe(plumber())
        .pipe(newer('target/gulp-dev-output'))
        .pipe(data(function(file) {
            var base_path = calculate_relative_path(srcBase + '/main/html', file.path);
            return {
                base_path: base_path,
                script_block: paths_to_script_tag_block(js_includes, base_path),
                style_block: paths_to_style_block(css_includes, base_path)
            };
        }))
        .pipe(template(template_data))
        .pipe(gulp.dest('target/gulp-dev-output'));
}

gulp.task('dev-compile-html', ['dev-compile-js', 'dev-compile-css'], getHTMLBuildPipeline);

gulp.task('dev-compile-html-safe', ['dev-compile-js-safe', 'dev-compile-css'], getHTMLBuildPipeline);

compile_tasks.push('dev-compile-html');

gulp.task('compile-html', ['compile-js', 'compile-css', 'dev-compile-html'], function () {
    var js_includes = ["js/vendor.min.js", "js/bundle.min.js"];
    var css_includes = ["css/style.css"];

    return gulp.src(srcBase + '/main/html/**/*.html')
        .pipe(plumber())
        .pipe(newer('target/gulp-output'))
        .pipe(data(function(file) {
            console.log(file);
            var base_path = calculate_relative_path(srcBase + '/main/html', file.path);
            return {
                base_path: base_path,
                script_block: paths_to_script_tag_block(js_includes, base_path),
                style_block: paths_to_style_block(css_includes, base_path)
            };
        }))
        .pipe(template(template_data))
        .pipe(htmlMin({
            removeComments: true,
            collapseWhitespace: true,
            collapseBooleanAttributes: true,
            removeAttributeQuotes: true,
            keepClosingSlash: true
        }))
        .pipe(gulp.dest('target/gulp-output'));
});
compile_tasks.push('compile-html');

gulp.task('default', compile_tasks, function () {
    //nothing to do
});

gulp.task('dev', ['default'], function () {
    gulp.watch('bower.json', ['bower']);
    gulp.watch(srcBase + '/main/less/**/*', ['dev-compile-css', 'dev-compile-html-safe']);
    gulp.watch(srcBase + '/main/images/**/*', ['dev-minify-images']);
    gulp.watch(srcBase + '/main/js/**/*', ['dev-compile-js-safe', 'dev-compile-html-safe']);
    gulp.watch(srcBase + '/main/html/**/*', ['dev-compile-html-safe']);
});
