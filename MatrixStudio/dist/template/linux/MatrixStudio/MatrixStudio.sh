#!/bin/sh
JAVA=java
BASEDIR=`dirname "$0"`
VMOPTIONS="-Xmx512m"
OPTIONS=""
exec $JAVA $VMOPTIONS -Djava.library.path=$BASEDIR/jars -jar jars/MatrixStudio.jar $OPTIONS
