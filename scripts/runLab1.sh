#!/bin/bash

echo "Compiling the program..."
javac lab1/*.java

TARGET_CLASS=""
if [ -n "$1" ]; then
    TARGET_CLASS=$1
else
    echo "Please specify the target class name. (e.g. Database1, Database2)"
    exit 1
fi

INPUT_FILE=""
if [ -n "$2" ]; then
    INPUT_FILE=$2
else
    echo "Please specify the input file. (e.g. ./testcases/lab1/db2_test1.txt, ./testcases/lab1/db2_test1.txt)"
    exit 1
fi

echo "Running the program..."
java lab1.$TARGET_CLASS < $INPUT_FILE
