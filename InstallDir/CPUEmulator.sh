#!/bin/sh
cd `dirname $0`
java -classpath "${CLASSPATH}:bin/classes/MainClasses.jar" CPUEmulatorMain $1
