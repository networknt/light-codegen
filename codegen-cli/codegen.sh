#!/bin/bash

set -ex

CURRENT_DIR=$PWD

if [ ! -f "$CURRENT_DIR/codegen-cli.jar" ]; then
    echo "codegen-cli.jar cannot be found!"

    exit 1
fi

if [ -z "$JAVA_HOME" ]; then
    echo "JAVA_HOME is not set"

    exit 1
fi

if [ ! -f "$CURRENT_DIR/rocker-compiler.conf" ]; then
    echo "generate config $CURRENT_DIR/rocker-compiler.conf"

    echo "rocker.template.dir=$CURRENT_DIR" > rocker-compiler.conf 
fi

if [ ! -d  "$CURRENT_DIR/target/classes" ]; then
    echo "create class folder $CURRENT_DIR/target/classes"

    mkdir -p "$CURRENT_DIR/target/classes"
fi


# Note: the order of the pathes in the classpath matters
# You need to change the path separator to ';' in order to run this script in git bash
JAVA_OPS="-cp .:target/classes/:codegen-cli.jar com.networknt.codegen.Cli"

# Note: please point JAVA_HOME to a JDK installation. JRE is not sufficient.
"$JAVA_HOME/bin/java" $JAVA_OPS "$@"

exit 0
