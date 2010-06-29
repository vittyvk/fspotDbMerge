#! /bin/sh

bin=`dirname "$0"`
bin=`cd "$bin"; pwd`

CLASSPATH=$CLASSPATH:$bin/../lib/sqlite4java.jar:$bin/../lib/java-getopt-1.0.13.jar:$bin/../build/
java -classpath $CLASSPATH fspotDbMerge $@
