#/bi/sh
perl -pe 's/\r\r|\r|\n/\n/g' functional_configuration.conf > functional_configuration.conf.bak
mv functional_configuration.conf.bak functional_configuration.conf
