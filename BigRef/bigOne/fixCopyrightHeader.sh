#!/bin/bash
find . -type d -name src | grep -v /node_modules/ | grep -v /bower_components/ | grep -v /target/ | xargs -I$$ find $$ \( -name \*.java -o -name \*.js -o -name \*.html \) -exec sed -i 's/(c)/©/gI' {} \+
find . -type d -name src | grep -v /node_modules/ | grep -v /bower_components/ | grep -v /target/ | xargs -I$$ find $$ \( -name \*.java \) | xargs -I$$ grep -L © $$ | xargs -I$$ ./addHeader.sh $$
