#!/bin/bash

set -ex

CURRENT_DIR=$PWD

if [ ! -f "$CURRENT_DIR/codegen-cli.jar" ]; then
    echo "File codegen-cli.jar not found!"

    exit 1
fi

if [ -z "$JAVA_HOME" ]; then
    echo "JAVA_HOME is not set"

    exit 1
fi

if [ ! -f "$CURRENT_DIR/rocker-compiler.conf" ]; then
    echo "File 'rocker-compiler.conf' not found! Generate default ..."

    echo "rocker.template.dir=$CURRENT_DIR" > rocker-compiler.conf 
fi

if [ ! -d  "$CURRENT_DIR/target/classes" ]; then
    echo "create class folder"

    mkdir -p "$CURRENT_DIR/target/classes"
fi



JAVA_OPS="-cp target/classes/:./codegen-cli.jar com.networknt.codegen.Cli"

$JAVA_HOME/bin/java $JAVA_OPS "$@"

exit 0
